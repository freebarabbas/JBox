# JBox
`JBox` is an **Archival Storage System** with **in-line deduplication** over internet, intended to backup data into Object Storage ( **Swift** ) and to **sync with multiple clients** on the fly.

What's JBox can do ?
=============
- **Archive via In-Line Deduplication**
- **Archive over the internet to ObjectStorage, Swift**
- **File Sync and ~~Share~~ with multi-clients** like Cloud Storage Service e.g. DropBox , GoogleDrive and OneDrive
- **Versioning ( Snapshot )**
- Auto Purge
- No Extra Installation Required, Fully Leverage OpenStack **Swift**,**Keystone**.
- No File System Watcher Library Required ( JBox has lightweight self-development inotify and combine it with file sync execution )
- ~~Virtual Storage Tiering ( Hot vs Cold )~~

What's technoloies JBox adopt ?
=============

`JBox` adopts `2-tier metadata structure` in order to effectively operate file system and allows to sync with multiple clients. During the file syncing, `copy on write(CoW)` makes sure metadata can be updated mutually exclusive and `Reference Counter` supports object purge to save more storage space. JBox reduces upload bandwidth and storage consumption by chunk compression and `variable chunk deduplication` which allows `Delta Sync` and `Versioning (Snapshot)` feature. JBox has `Dedup-Map` to make archive configurable to fit different kinds of backup stream. It does not only control the `Dedup Anchor` for numbers of the chunks per file but also provide different kinds of deduplication skins, to try to balance between efficiency and performance.

`JBox` adopts the technologies and provides the features as below.
- **JBox fully leverage OpenStack**
  - Using [Swift](http://docs.openstack.org/developer/swift/ "") as Repository
  - Using [KeyStone](http://docs.openstack.org/developer/keystone/ "") as Access Control
- `2-tier metadata structure` to make file system operation effectively and allows to sync with multiple clients.
  - 2-tier metadata structure can provide `light weight inotify` feature combine with `file sync execution`.
  - file sync is with `multiple clients` and always make a `newest backup copy in ObjectStorge, Swift`.
- `COW (copy on write)` make sure metadata update mutual exclusion 
- It's `chunk-level variable deduplication` by default which allows backup stream has 
  `Delta Sync` and `Versioning (Snapshot)` feature. 
  - Delta Sync is only transfer the chunk content the modificatioin.
- It's `in-line deduplication`, which is dedup before saving the data.
- JBox `compresses` the chunk (object) before upload which reduces bandwidth 
  and Object Storage, Swift consumption. 
- JBox use `dedup-map` to make archive configurable, it allows to configure as below.
  - `Dedup Anchor` for number of the chunks per file
  - `Refector` limit interval for Dedup Anchor growing
  - `File Level Deduplication` vs. `Chunk Level Deduplication`
  - `Fixed Chunking` vs. `Variable Chunking` Deduplication
    - In Config.java and will allow to maintain dedup-map.cfg for user to adjust dynamically.
- It's using reference counter to support `metadata and object purge`.
  - Purge leadtime for chunk level metadata ( fxxxxx )
  - Purge leadtime for object ( c0xxxxx or c1xxxxx )
  - Rename purged object as cold storage tier, if no further reference, then purge, if objecdt get refernece again, then rename it back w/o upload.
- `Virtual Storage Tiering` when screen the exisitng chunk, scan `Hot Chunks` first which is chunk(object) being reference at least one in Swift, if can't find it then move to `Cold Chunk`, if screen can't find in both then upload new chunk to Swift.
  - Phase 1: Hot Chunk is existing referenced chunk, Cold Chunkk is purged chunk but haven't delete in Swift. Dedup Screen from Hot to Cold.
    - Done
  - ~~Phase 2: Hot Chunk is the chunk been referenced with certain time ( e.g. 3 month ), Cold Chunnk is other than that existing referneced chunk, plus Purged Chunk is the purged chunk but haven't delete in Swift yet. Dedup Screen from Hot to Cold, then Cold to Purged.~~
    - Open
- `File Share` feature means share your file to other people which means JBox can generate a temp link and people can download from link.
  - ~~Extra Web Service to repeat "download specific version" function since other people need to get the file from internet.~~
  - ~~Extra DB to maintain the link, availabe time ( expire date ) and privilege ... etc.~~
    - Open

How to run JBox ?
=============
## Execution
For run JBox, you need to have a OpenStack Environement, Swfit All In One aka (SAIO) is an option if you didn't want to purchase any public cloud solution. The SAIO setup can be found in [SAIO](http://docs.openstack.org/developer/swift/development_saio.html ""). or my post before [OpenStack - Swift Dev Box - SAIO on Ubuntu 14.04 via VirtualBox](http://chianingwang.blogspot.com/2015/01/openstack-swift-dev-box-saio-on-ubuntu.html "").
- (1) Install Java
  - [how to install Linux 32 bit Java](https://java.com/en/download/help/linux_install.xml "").
  - [how to install Linux 64 bit Java](https://java.com/en/download/help/linux_x64_install.xml "").

- (2) Find the code localtion and copy c++ *.so ( shared object ) under /usr/lib/
```bash
$ sudo cp ./dll/libclsJavaVariableChunk.so ./usr/lib/*
or 
$ cp ./dll/libclsJavaVariableChunk.so /tmp/*
```
if you have question about reference the *.so in java you can reference this post. 
  - [how to reference c lib in java via jni](http://chianingwang.blogspot.com/2015/09/how-to-reference-c-lib-in-java-via-jni.html "how to reference c lib in java via jni").

- (3) Find the code localtion and make sure it's has execute permission
```bash
$ ./JBox.jar
bash: ./JBox.jar: Permission denied
$ sudo chmod +x ./JBox.jar
```

- (4) parameters definition
  - a. Username in KeyStone (Tenant/User) for Swift
  - b. Password in KeyStone (Tenant/User) for Swift
  - c. Deduplication Algorithm, var=variable chunk ( content aware ), fix=fix chunk and no=no chunnk , it's file level
  - d. divider have to be number base on power of 2, 
<pre>**divider=64 example**<code> 
e.g. divider = 64 
then file size / 64 and get between lower bound power of 2 to upper bound power of 2, 
then Dedup Anchor = upper bound of power of 2. 
Deduplication average size will be around Dedup Anchor.
</code></pre>
<p>Here is pseudo code concept</p>
<pre>**pesudo code**<code>
if var in c, 
then 
  chunk size will be 0.85 x Dedup Anchor ~ 2 x Dedup Anchor
  number of chunk between 32 ~ 75
else if fix in c, 
then 
  chunk size will be Dedup Anchor
  number of chunk will <= 64
</code></pre>
  - e. refactor=0 which is no refactor or any number n
<p>Dedup Anchor 2^x will be wipe out if new Dedup 2^y, then (2^y) / (2^x) > n </p>
<pre>**refactor=3 example**<code>
e.g. if Dedup Anchor = 18 , then JBox will divide file size by 2^18, 
however if file grow and when we found file size's power of 2 upper bound is 2^22, 
then (2^22)/(2^18) = 4 > 3, then JBox Dedup Anchor will be wiped out then use 22 as Dedup Anchor.
</code></pre>
  - f. refcounter flag, if we would like to turn on then set 1, otherwise 0

- (5) Try first time and find the parameters
```bash
$ ./JBox.jar 
try ... #java -jar JBox.jar <username> <password> <var || fix || no> / 
<divider= 16 || 32 || 64 || 128 ... > /
<refactor= 0(no refactor) ... || 3 || 4 || 5 ...> /
<refcounter off(0) or on(1)>
``` 

- (6) run JBox.jar via ** $ java -jar JBox.jar with arguements ** ... or ** $ ./JBox.jar with arguements ** 
```bash
$ ./JBox.jar var var var 64 0 0
username:var, password:var, dedup-alg:var, divider:64, refactor:0
2016/01/30 15:31:00.507: Connecting to server
2016/01/30 15:31:01.509: Syncing (upload/download/copy/overwrite) /home/johnny/JBox
2016/01/30 15:31:02.510: Syncing (upload/download/copy/overwrite) /home/johnny/JBox
2016/01/30 15:31:03.510: Syncing (upload/download/copy/overwrite) /home/johnny/JBox
2016/01/30 15:31:04.512: Syncing (upload/download/copy/overwrite) /home/johnny/JBox
2016/01/30 15:31:05.513: All to update
2016/01/30 15:31:06.513: All to update
2016/01/30 15:31:07.514: All to update
2016/01/30 15:31:08.514: All to update
2016/01/30 15:31:09.515: All to update
2016/01/30 15:31:10.515: Getting the chunk list from server
2016/01/30 15:31:11.516: All to update
```
- (7) check JBox Sync Folder and local Metadata and Log
  - a. Sync Folder will be /home/{user}/JBox
  - b. local Metadata always name with Swift Username (e.g. usr) under /home/{user}/JBoxLog
  - c. Log is under under /home/{user}/JBoxLog as well and it's html and follow the timestamp everytime you restart the code.
```bash
$ cd JBox
$ ls
$ cd ../JBoxLog/
$ ls
usr  run20160127094643.html  run20160127100347.html  
```

How to join JBox coding ?
=============
## Installation and Setup
JBox is the Java code which is composed with `Eclipse IDE`. It's Eclipse project and easy to debug and run.
Here is the steps how to open it in eclipse.

- (1) download the JBox source code or import into Eclipse directly
```bash
$ git clone https://github.com/chianingwang/JBox.git
$ cd ./JBox
```
    - In eclipse, right click at Package Explore: Import --> Git --> Project from Git --> Clone URl then paste "https://github.com/chianingwang/JBox.git" --> next --> master --> next --> Import existing projects --> next , then done if you miss the project file you can find .prject and .classpath under prj folder.
    - ![Alt text](https://github.com/chianingwang/JBox/blob/master/img/Import_JBox.png "Import JBox in eclipse")

- (2) double check reference library 
  - double check required lib
  - ![Alt text](https://github.com/chianingwang/JBox/blob/master/img/Required_lib.png "Double Check Required Library (JAR)")
- (3) add run/debug configuration 
  right click project and select "run configurations" --> "New Launcha Configuration" --> Argument --> Program arguments: 
  - Setup Run Paramenter: e.g. usr pwd var 64 0 0
  - Enlarge the Java VM cache size: VM arguements : -Xms1024m -Xmx2048m
  - ![Alt text](https://github.com/chianingwang/JBox/blob/master/img/Required_Para.png "Configure Run Paramenters")

- (3) reference required *.so ( c++ ) object 
  - Add Library reference path
  - ![Alt text](https://github.com/chianingwang/JBox/blob/master/img/Required_obj.png "Configure Reference Object Directory")

- (4) run the TestforSynvV3.java the console in eclipse might show as below
```bash
username:usr, password:pwd, dedup-alg:var, divider:64, refactor:0
2016/01/30 17:07:20.407: Start
2016/01/30 17:07:21.408: Connecting to server
2016/01/30 17:07:22.409: Connecting to server
2016/01/30 17:07:23.409: Connecting to server
2016/01/30 17:07:24.410: Connecting to server
2016/01/30 17:07:25.412: Getting the chunk list from server
```

For the 2-Tier Metadata and what's the algorithm logic to identify new/update/copy/rename/move/delete can be found in here.
  - [Archival and Sync via ObjectStorage Swift - JBox](http://chianingwang.blogspot.com/2016/01/archival-and-sync-via-objectstorage.html "Archival and Sync via ObjectStorage Swift - JBox"). explain, why JBox doesn't need to adopt any extra library to do the thing like Linux inotify. In such, JBox doesn't need to reference specific file system monitor library such as FileSystemWatcher in Windows for C# or JNotify in Linux for Java.

  # **Copyright (c) 2016 Chianing Wang**

==========
Technology
==========

In this sections we would like to discuss the technologies we applied in JBox.

--------------------------------
What's technologies JBox adopt ?
--------------------------------

**JBox** adopts **2-tier metadata structure** in order to effectively operate file system and allows to sync with multiple clients. During the file syncing, **copy on write(CoW)** makes sure metadata can be updated mutually exclusive and **Reference Counter** supports object purge to save more storage space. JBox reduces upload bandwidth and storage consumption by chunk compression and **variable chunk deduplication** which allows **Delta Sync** and **Versioning (Snapshot)** feature. **JBox** has **Dedup-Map** to make archive configurable to fit different kinds of the backup stream. It does not only control the **Dedup Anchor** for numbers of the chunks per file but also provide different kinds of deduplication skins, to try to balance between efficiency and performance.

**JBox** adopts the technologies and provides the features as below.
  - **JBox fully leverage OpenStack swift**
  - Using `Swift <http://docs.openstack.org/developer/swift/>`_ as Repository
  - Using `KeyStone <http://docs.openstack.org/developer/keystone/>`_ as Access Control
  - **2-tier metadata structure** to make file system operates effectively and allows to sync with multiple clients.
  - **2-tier metadata structure** can provide **light weight inotify** feature to trigger file sync execution.
  - file sync is with **multiple clients** and always make a **newest backup copy in ObjectStorge, Swift**.
  - **COW (copy on write)** make sure metadata update mutual exclusion
  - It's **chunk-level variable deduplication** by default which allows backup stream has **Delta Sync** and **Versioning (Snapshot)** feature.
  - Delta Sync only transfers the chunk containing the modification.
  - It's **in-line deduplication**, which is dedup before saving the data.
  - JBox **compresses** the chunk (object) before upload which reduces bandwidth and Object Storage, Swift consumption.
  - JBox use **dedup-map** to make archive configurable, it allows to configure as below.
  - **Dedup Anchor** for number of the chunks per file
  - **Refector** limit interval for Dedup Anchor growing
  - **File Level Deduplication** vs. **Chunk Level Deduplication**
  - **Fixed Chunking** vs. **Variable Chunking** Deduplication
  - In Config.java and will allow maintaining dedup-map.cfg for the user to adjust dynamically.
  - It's using reference counter to support `metadata and object purge`.
  - Purge lead time for chunk level metadata ( fxxxxx )
  - Purge lead time for object ( c0xxxxx or c1xxxxx )
  - Rename purged object as the cold storage tier, if no further reference, then purge, if objects get reference again, then rename it back w/o upload.
  - **Virtual Storage Tiering** when screen the existing chunk, scan **Hot Chunks** first which is chunk(object) being the reference at least one in Swift, it can't find it then move to **Cold Chunk**, if screen can't find in both then upload new chunk to Swift.
  - Phase 1: Hot Chunk is existing referenced chunk, Cold Chunk is purged chunk but hasn't delete in Swift. Dedup Screen from Hot to Cold.
  - Phase 2: Hot Chunk is the chunk been referenced with certain time ( e.g. 3 month ), Cold Chunnk is other than that existing referneced chunk, plus Purged Chunk is the purged chunk but haven't delete in Swift yet. Dedup Screen from Hot to Cold, then Cold to Purged.

For the 2-Tier Metadata and what's the algorithm logic to identify **new/update/copy/rename/move/delete** can be found in here.
  - `Archival and Sync via ObjectStorage Swift - JBox <http://chianingwang.blogspot.com/2016/01/archival-and-sync-via-objectstorage.html>`_. explain, why JBox doesn't need to adopt any extra library to do the thing like Linux inotify. In such, JBox doesn't need to reference specific file system monitor library such as FileSystemWatcher in Windows for C# or JNotify in Linux for Java.

---------------------------
dedup parameters definition
---------------------------

a. Deduplication Algorithm, var=variable chunk ( content aware ), fix=fix chunk and no=no chunk, it's file level

b. divider have to be number base on power of 2

.. code-block:: console

    # divider=64 example
    # e.g. divider = 64
    # then file size / 64 and
    # get between lower bound power of 2 to upper bound power of 2,
    # then Dedup Anchor = upper bound of the power of 2.
    # Deduplication average size will be around Dedup Anchor.
    # Here is pseudo code concept
    if var in c,
    then
      chunk size will be 0.85 x Dedup Anchor ~ 2 x Dedup Anchor
      number of chunk between 32 ~ 75
    else if fix in c,
    then
      chunk size will be Dedup Anchor
      number of chunk will <= 64

c. refactor=0 which is no refactoring or any number n

.. code-block:: console

    # Dedup Anchor 2^x will be wipe out if new Dedup 2^y,
    # then (2^y) / (2^x) > n </p>
    # refactor=3 example
    # e.g. if Dedup Anchor = 18 ,
    # then JBox will divide file size by 2^18,
    # however if file grow and when we found file size
    # is power of 2 upper bound is 2^22,
    # then (2^22)/(2^18) = 4 > 3, then
    # JBox Dedup Anchor will be wiped out
    # then use 22 as Dedup Anchor.

d. refcounter flag, if we would like to turn on then set 1, otherwise 0.

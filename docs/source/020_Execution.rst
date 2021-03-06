=========
Execution
=========

-----------------
How to run JBox ?
-----------------

^^^^^^^^^^^^^^^^^^^^^^^^^^
1.Get Swift and has Access
^^^^^^^^^^^^^^^^^^^^^^^^^^

^^^^^^^^^^^^^^
2. Copy C++ so
^^^^^^^^^^^^^^

Find the code location and copy c++ ``*.so ( shared object ) under /usr/lib/``

.. code-block:: console

    $ sudo cp ./dll/libclsJavaVariableChunk.so ./usr/lib/*
    or
    $ cp ./dll/libclsJavaVariableChunk.so /tmp/


if you have a question about reference the ``*.so`` in java you can reference this post.
  - `how to reference c lib in java via jni <http://chianingwang.blogspot.com/2015/09/how-to-reference-c-lib-in-java-via-jni.html>`_.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
3. Prepare JBox Configuration
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Prepare JBox Configuration ``JBoxconfig.properties`` with the JBox executable in the same Directory

.. code-block:: console

    # syncfolders=/home/vagrant/Yuan/JBoxGenReport_V4/syncfolder
    syncfolders=/tmp/JBox

    # JBox Properties
    # swift auth url
    authurl=https://www.xxx.com/auth/v1.0
    # swift username
    username=xxx
    # swift password
    password=xxx

    # swift container, div, ext, pow, others
    # if div then container name rule will be
    # file-extension_type_power_div, e.g: pdfvar24128
    # else if ext then container name rule will be
    # file-extension_type_power, e.g: pdfvar24
    # else if pow the cotnainer name rule will be type_power, e.g: var24
    # else if others then container name rule will be
    # others - put all the chunks into one container e.g: dedupcontainer
    # else will be default pow
    containername=GenTestNew

    # sync time is milliseconds = 1/1000 seconds,
    # 5000 milliseconds = 5 seconds
    # if p: push mode, then means every sync time
    # e.g. 30 min 30*60*1000=1800000 will re-sync
    synctime=5000

    # s: sync, q: query, r: retrive
    # dedup algorithm,
    # no - no deduplication, fix - fix chunking, var -variable chunking
    type=var

    # divider can be 32, 64, 128...2^n,
    # if fix and var algorithm then use divider=0 or 1
    divider=128

    # power default is 0,
    # if you prefer specific anchor then you can assigned it
    # 10 = 2^10 as anchor
    # if type is fix then fix size 2^10
    # if type is var
    # then var size is between 0.85 * 2^10 ~ 2 * 2^10
    power=0

    # refactor=0 is
    # no refactor,
    # 1 is refresh all the time,
    # 2 is every 2^x/2^y = 2 then refactor mod
    refactor=0

    # extra parameters
    # maximum multiplier
    min=0.25
    # minimum multiplier
    max=32.0

    # refcounter,
    # -1: true deletion, 0 : off, 1 : on,
    # if > 1 such as 2, 3, 4 ... ~
    # means you have more than one client need to deal with.
    # if it's -1 means delete right away,
    # but this is only for push scenario and no multi clients
    # if it's 0 means won't add auto purge feature
    # when deleting the object and will keep chunks c+hash forever
    # if it's 1 then move all deleted object to backup
    # and give X-Delete-At <object purge seconds>
    # if it's 2~n, then same with 1 but apply
    # how many clients you have
    refcounter=-1

    # customized min and max instead of calculate by
    # mod = size / 64, min=0.85*mod and max=2*mod
    clientnum=1

    # runmode: 0: master mode,
    # only upload to object storage, 1: slaves mode which can sync
    runmode=0

^^^^^^^^^^^^^^^^^^^^^^^^^^
4. run JBox with arguments
^^^^^^^^^^^^^^^^^^^^^^^^^^

.. code-block:: console

    $ JBox <p, s, r, q> or <help>

"""""""""""""""""
Command Line Help
"""""""""""""""""

   - More detail you can try ``$ JBox h``

^^^^^^^^^^^^^^^
PS: Setup Swift
^^^^^^^^^^^^^^^

   - For run JBox, you need to have an OpenStack Environment, Swift All In One aka (SAIO) is an option if you didn't want to purchase any public cloud solution. The SAIO setup can be found in `SAIO <http://docs.openstack.org/developer/swift/development_saio.html>`_. or my post before `OpenStack - Swift Dev Box - SAIO on Ubuntu 14.04 via VirtualBox <http://chianingwang.blogspot.com/2015/01/openstack-swift-dev-box-saio-on-ubuntu.html>`_.

^^^^^^^^^^^^^^^^
PS: Install Java
^^^^^^^^^^^^^^^^

  - `how to install Linux 32 bit Java <https://java.com/en/download/help/linux_install.xml>`_.
  - `how to install Linux 64 bit Java <https://java.com/en/download/help/linux_x64_install.xml>`_.

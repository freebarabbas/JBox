===========
Development
===========

-------------------------
How to join JBox coding ?
-------------------------

JBox is the Java code which is composed with ``Eclipse IDE``. It's Eclipse project and easy to debug and test.
Here are the steps how to open it in eclipse.

^^^^^^^^^^^^^^^^^^^^^^
Installation and Setup
^^^^^^^^^^^^^^^^^^^^^^

(1) download the JBox source code or import into Eclipse directly

.. code-block:: console

    $ git clone https://github.com/chianingwang/JBox.git
    $ cd ./JBox

In eclipse, right click at Package Explore: ``Import --> Git --> Project from Git --> Clone URl`` then paste **https://github.com/chianingwang/JBox.git** ``--> next --> master --> next --> Import existing projects --> next`` , then done if you miss the project file you can find .prject and .classpath under prj folder.

  - `Import JBox in eclipse <https://github.com/chianingwang/JBox/blob/master/img/Import_JBox.png>`_

(2) double check reference library

  - double check required lib
  - `Double Check Required Library (JAR) <https://github.com/chianingwang/JBox/blob/master/img/Required_lib.png>`_.

(3) add run/debug configuration

Right click project and select ``run configurations --> New Launcha Configuration --> Argument --> Program arguments``:
  - Setup Run Paramenter: e.g. usr pwd var 64 0 0
  - Enlarge the Java VM cache size: VM arguements : -Xms1024m -Xmx2048m
  - `Configure Run Paramenters <https://github.com/chianingwang/JBox/blob/master/img/Required_Para.png>`_.

(4) reference required ``*.so ( c++ ) object``

  - Add Library reference path
  - `Configure Reference Object Directory <https://github.com/chianingwang/JBox/blob/master/img/Required_obj.png>`_.

(5) Start to debug or run JBox

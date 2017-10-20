=========
Algorithm
=========

This section we would like to talk about the algorithm we adopt in JBox.

---------------------------------
Deduplicatioin Chunking Algorithm
---------------------------------

Mainly purpose for JBox is back up your data from local to Object Storage,
thus we adopt compression and deduplication to reduce as much as possible your backup data set
on the remote repository which is Object Storage, OpenStack Swift.

^^^^^^^^^^^^
Fix Chunking
^^^^^^^^^^^^

When we do the deduplication, the chunk size is all fixed.

^^^^^^^^^^^^^^^^^
Variable Chunking
^^^^^^^^^^^^^^^^^

When we do the deduplication, the chunk size is variable
which means it will change base on the backup data stream content.

^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^
Dynamic Anchor Variable Chunking
^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^^

Like we learn from the previous section, even the chunk size is variable
but we still need boundary to limit the chunk size.
The **Dynamic Anchor Variable Chunking** is base on the file size
and compression ratio to dynamic decide Variable Chunk Boundary but keep it as Anchor in metadata,
when file content change, deduplication will always apply the same rule.

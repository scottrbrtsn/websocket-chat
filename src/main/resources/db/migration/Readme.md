# This folder contains scripts to migrate the database with Flyway
# This file must remain here while there are no sql scripts to run so that flyway has a directory to look in.
# Flyway will do nothing when it sees this empty directory, but this director must exist.
# Git will not track a completely empty directory and will not create the empty directory on other machines. Therefore, this file must exist

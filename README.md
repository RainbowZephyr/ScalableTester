# Scalable Tester

A tool designed to test the students code against some tests. The tool scans a given directory for archive files (zip, rar, gz, xz) and automatically extracts the archives, and moves all files ending with a certain extension to the root of each extracted archive. The tool is designed to use a low memory foot print, scale easily with threads on the processor and kill the student's code incase it takes longer than a certain period. **Memory bombs are not handled**.

The tool is built using maven and has a CLI, code has been supplied to serve as an example.

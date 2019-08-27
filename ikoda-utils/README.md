# ikodaCsvLibsvmCreator

ikodaCsvLibsvmCreator collects data into a simple dataframe. 

It saves the output to file in CSV or LIBSVM format. It can also divide the data into a CSV and a LIBSVM component.

In addition, ikodaCsvLibsvmCreator streams the data to a Spark cluster.

### ikodaCsvLibsvmCreator :

1. Collects data dynamically. New columns create on the fly.
1. Maintains a UID for each column.
1. Maintains a category or label for each row.
1. Maintains human readable text column and label names for sparse (LIBSVM) data
1. Runs thread safe instances for the synchronous collection of distinct datasets.
1. Saves data as a CSV or LIBSVM (either appending or overwriting). 
1. Divides the data for saving into LIBSVM and CSV components.
1. Opens Data in CSV or LIBSVM format.
1. Merges CSV files.
1. Merges LIBSVM files.
1. Streams data to a Spark cluster
1. Emails the data in CSV format



### see <a href="https://github.com/amerywu/ikodaCsvLibsvmCreator/wiki">Simple Guide for details</a>


#### <a href="https://amerywu.github.io/ikodaCsvLibsvmCreator/javadoc/ikoda/utils/Spreadsheet.html">API docs</a>

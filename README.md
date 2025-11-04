# Spark WordCount Application - Complete Guide

A comprehensive guide to building and running a distributed word count application using Apache Spark on YARN with HDFS storage.

## 📋 Table of Contents

- [Overview](#overview)
- [Prerequisites](#prerequisites)
- [Environment Setup](#environment-setup)
- [Part 1: Building the JAR on MacBook](#part-1-building-the-jar-on-macbook)
- [Part 2: Transferring JAR to Ubuntu VM](#part-2-transferring-jar-to-ubuntu-vm)
- [Part 3: Setting Up Hadoop Services on Ubuntu](#part-3-setting-up-hadoop-services-on-ubuntu)
- [Part 4: Preparing Input Data](#part-4-preparing-input-data)
- [Part 5: Running the Spark Application](#part-5-running-the-spark-application)
- [Part 6: Viewing Results](#part-6-viewing-results)
- [Understanding the Spark Submit Command](#understanding-the-spark-submit-command)
- [Troubleshooting](#troubleshooting)
- [Project Structure](#project-structure)

---

## 🎯 Overview

This project demonstrates a distributed word count application that:
- ✅ Built using Java and Maven on MacBook
- ✅ Deployed to Ubuntu VMware virtual machine
- ✅ Reads text files from HDFS (Hadoop Distributed File System)
- ✅ Processes data using Apache Spark on YARN cluster
- ✅ Writes results back to HDFS

### Architecture
```
MacBook (Development) → Ubuntu VM (Production)
                          ↓
                    Hadoop HDFS (Storage)
                          ↓
                    Spark on YARN (Processing)
                          ↓
                    HDFS (Results)
```

---

## 🔧 Prerequisites

### On MacBook (Development Machine)

| Software | Version | Purpose |
|----------|---------|---------|
| Java JDK | 1.8+ | Compile Java code |
| Apache Maven | 3.x | Build project and create JAR |
| IntelliJ IDEA | Latest | IDE for development (optional) |

### On Ubuntu VM (Production Machine)

| Software | Version | Installation Path | Purpose |
|----------|---------|-------------------|---------|
| Java JDK | 1.8 | `/usr/lib/jvm/java-8-openjdk-arm64` | Runtime environment |
| Apache Hadoop | 2.9.0 | `~/Downloads/hadoop-2.9.0` | HDFS & YARN |
| Apache Spark | 3.5.7 | `~/Downloads/spark-3.5.7-bin-hadoop3` | Data processing |

### Verify Prerequisites on Ubuntu

```bash
# Check Java
java -version

# Check Hadoop
hadoop version

# Check Spark
spark-submit --version

# Check all running services
jps
```

---

## ⚙️ Environment Setup

### Configure Ubuntu Environment Variables

Edit your `~/.bashrc` file:

```bash
nano ~/.bashrc
```

Add the following configuration:

```bash
# Java environment
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-arm64

# Hadoop environment
export HADOOP_HOME=$HOME/Downloads/hadoop-2.9.0
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
export YARN_CONF_DIR=$HADOOP_HOME/etc/hadoop

# Hive environment (optional)
export HIVE_HOME=$HOME/Downloads/apache-hive-2.3.5-bin

# Spark environment
export SPARK_HOME=$HOME/Downloads/spark-3.5.7-bin-hadoop3

# Update PATH
export PATH=$PATH:$HADOOP_HOME/bin:$HADOOP_HOME/sbin
export PATH=$PATH:$HIVE_HOME/bin
export PATH=$PATH:$SPARK_HOME/bin:$SPARK_HOME/sbin

echo "Welcome!!"
```

Save and reload:

```bash
# Save: Ctrl+X, then Y, then Enter
# Reload configuration
source ~/.bashrc

# Verify environment variables
echo "HADOOP_HOME: $HADOOP_HOME"
echo "HADOOP_CONF_DIR: $HADOOP_CONF_DIR"
echo "SPARK_HOME: $SPARK_HOME"
```

---

## 🏗️ Part 1: Building the JAR on MacBook

### Step 1: Create Project Structure

```
Spark_Submit_Demo/
├── pom.xml
├── README.md
└── src/
    └── main/
        └── java/
            └── com/
                └── example/
                    └── Spark_Submit_Demo/
                        └── WordCount.java
```

### Step 2: Build with Maven

```bash
# Navigate to project directory
cd /path/to/Spark_Submit_Demo

# Clean and build
mvn clean package

# Verify JAR was created
ls -lh target/
```

**Expected Output:**
```
spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar  ✅ Use this file
spark_example-0.0.1-SNAPSHOT.jar
```

### Step 3: Locate the JAR File

```bash
# JAR location
target/spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

**File Size:** Approximately 30-50 MB (includes all dependencies)

---

## 📦 Part 2: Transferring JAR to Ubuntu VM

### Method A: Using SCP (Secure Copy)

```bash
# From MacBook terminal
# Syntax: scp <source> <user>@<host>:<destination>

scp target/spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar danny@<ubuntu-vm-ip>:/home/danny/

# Example with IP address
scp target/spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar danny@192.168.1.100:/home/danny/
```

**Enter password when prompted**

### Method B: Using Shared Folder (VMware)

1. **Enable Shared Folders in VMware:**
   - VM → Settings → Options → Shared Folders
   - Enable shared folders
   - Add MacBook folder path

2. **Copy JAR to Shared Folder:**
   ```bash
   # On MacBook
   cp target/spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar /path/to/shared/folder/
   ```

3. **Access from Ubuntu:**
   ```bash
   # On Ubuntu VM
   cp /mnt/hgfs/shared_folder/spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar /home/danny/
   ```

### Method C: Using USB Drive or Direct Copy

1. Copy JAR to USB drive on MacBook
2. Mount USB in Ubuntu VM
3. Copy to home directory

### Step 4: Verify JAR on Ubuntu

```bash
# On Ubuntu VM
cd /home/danny

# List and verify file
ls -lh spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar

# Check file size (should be 30-50 MB)
du -h spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar
```

---

## 🚀 Part 3: Setting Up Hadoop Services on Ubuntu

### Step 1: Start HDFS (Hadoop Distributed File System)

```bash
# Start HDFS daemons
start-dfs.sh

# Verify HDFS is running
jps | grep -E "NameNode|DataNode"
```

**Expected Output:**
```
12345 NameNode
12346 DataNode
12347 SecondaryNameNode
```

**Access HDFS Web UI:**
- Open browser: `http://localhost:50070`
- Or: `http://<ubuntu-vm-ip>:50070`

### Step 2: Start YARN (Resource Manager)

```bash
# Start YARN daemons
start-yarn.sh

# Verify YARN is running
jps | grep -E "ResourceManager|NodeManager"
```

**Expected Output:**
```
12348 ResourceManager
12349 NodeManager
```

**Access YARN Web UI:**
- Open browser: `http://localhost:8088`
- Or: `http://<ubuntu-vm-ip>:8088`

### Step 3: Verify All Services

```bash
# Check all running Hadoop services
jps
```

**Complete Expected Output:**
```
12345 NameNode
12346 DataNode
12347 SecondaryNameNode
12348 ResourceManager
12349 NodeManager
12350 Jps
```

### Step 4: Check HDFS Health

```bash
# Check HDFS report
hdfs dfsadmin -report

# Check HDFS filesystem
hdfs dfs -ls /
```

---

## 📝 Part 4: Preparing Input Data

### Step 1: Create Sample Input File (if not exists)

```bash
# Navigate to home directory
cd /home/danny

# Create sample input file
cat > words.txt << 'EOF'
Hello World
Hello Spark
Apache Spark is amazing
Spark runs on Hadoop YARN
Big Data processing with Spark
Hadoop HDFS stores data
Spark Spark Spark
Hello Big Data World
YARN manages cluster resources
Distributed computing with Apache Spark
EOF

# View the file
cat words.txt
```

**Or use your existing `words.txt` file**

### Step 2: Create Input Directory in HDFS

```bash
# Create input directory in HDFS
hdfs dfs -mkdir -p hdfs://localhost:50000/input/

# Verify directory creation
hdfs dfs -ls hdfs://localhost:50000/
```

**Expected Output:**
```
Found 1 items
drwxr-xr-x   - danny supergroup          0 2025-11-04 21:00 hdfs://localhost:50000/input
```

### Step 3: Upload Input File to HDFS

```bash
# Upload file from local to HDFS
hdfs dfs -put /home/danny/words.txt hdfs://localhost:50000/input/words.txt

# Verify upload
hdfs dfs -ls hdfs://localhost:50000/input/
```

**Expected Output:**
```
Found 1 items
-rw-r--r--   1 danny supergroup        256 2025-11-04 21:00 hdfs://localhost:50000/input/words.txt
```

### Step 4: Verify File Content in HDFS

```bash
# View file content in HDFS
hdfs dfs -cat hdfs://localhost:50000/input/words.txt
```

**Expected Output:**
```
Hello World
Hello Spark
Apache Spark is amazing
...
```

### HDFS File Status

```bash
# Check file details
hdfs dfs -stat "%n - %b bytes - %r replicas" hdfs://localhost:50000/input/words.txt
```

---

## 🎯 Part 5: Running the Spark Application

### Step 1: Navigate to Spark Directory

```bash
cd ~/Downloads/spark-3.5.7-bin-hadoop3
```

### Step 2: Submit Spark Job to YARN

```bash
bin/spark-submit \
  --class com.example.Spark_Submit_Demo.WordCount \
  --master yarn \
  --deploy-mode cluster \
  --num-executors 1 \
  /home/danny/spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
  hdfs://localhost:50000/input/words.txt \
  hdfs://localhost:50000/output/wordcount
```

### Alternative: Submit Spark Job to Standalone Spark Cluster

If you don't want to use YARN (Hadoop) and prefer to use standalone Spark instead:

**Step 1: Find Your Spark Master URL**

Open your browser and navigate to the Spark Web UI:
```
http://localhost:8080/
```

Look for the **URL** field at the top of the page. It will look something like:
```
spark://ubuntuserver:7077
```

**Submit Job to Standalone Spark**

Replace `--master yarn` with the Spark master URL you found:

```bash
bin/spark-submit \
  --class com.example.Spark_Submit_Demo.WordCount \
  --master spark://ubuntuserver:7077 \
  --deploy-mode cluster \
  --num-executors 1 \
  /home/danny/spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
  hdfs://localhost:50000/input/words.txt \
  hdfs://localhost:50000/output/wordcount
```

**Key Differences:**

| Aspect | YARN Mode | Standalone Spark Mode |
|--------|-----------|----------------------|
| Master Parameter | `--master yarn` | `--master spark://HOST:PORT` |
| Resource Manager | Hadoop YARN | Spark Master |
| Web UI | `http://localhost:8088` | `http://localhost:8080` |
| Services Required | HDFS + YARN | HDFS + Spark Master/Workers |

**Note:** Before using standalone mode, ensure your Spark standalone cluster is running:

```bash
# Start Spark Master
$SPARK_HOME/sbin/start-master.sh

# Start Spark Worker(s)
$SPARK_HOME/sbin/start-worker.sh spark://ubuntuserver:7077

# Verify services are running
jps | grep -E "Master|Worker"
```

Expected output:
```
12345 Master
12346 Worker
```

### Step 3: Monitor Job Execution

**Console Output (Cluster Mode):**
```
25/11/04 21:05:00 INFO Client: Application report for application_1760524837247_0013 (state: RUNNING)
25/11/04 21:05:10 INFO Client: Application report for application_1760524837247_0013 (state: FINISHED)
	 final status: SUCCEEDED
	 tracking URL: http://ubuntuserver:8088/cluster/app/application_1760524837247_0013
```

**Check YARN Web UI:**
1. Open browser: `http://localhost:8088`
2. Find your application: "WordCount"
3. Click on Application ID to view details
4. Check Status: Should show "FINISHED" and "SUCCEEDED"

**View Application Logs:**
```bash
# Get application ID from console output or YARN UI
yarn logs -applicationId application_1760524837247_0013

# Or save to file
yarn logs -applicationId application_1760524837247_0013 > spark_job_logs.txt
```

---

## 📊 Part 6: Viewing Results

### Step 1: List Output Directory

```bash
# List output directory
hadoop fs -ls /output/wordcount
```

**Expected Output:**
```
Found 3 items
-rw-r--r--   1 danny supergroup          0 2025-11-04 21:10 /output/wordcount/_SUCCESS
-rw-r--r--   1 danny supergroup        123 2025-11-04 21:10 /output/wordcount/part-00000
-rw-r--r--   1 danny supergroup        145 2025-11-04 21:10 /output/wordcount/part-00001
```

**File Explanation:**
- `_SUCCESS` - Indicates job completed successfully (empty file)
- `part-00000` - Output from first executor
- `part-00001` - Output from second executor

### Step 2: View Individual Output Parts

```bash
# View first part
hadoop fs -cat /output/wordcount/part-00000

# View second part
hadoop fs -cat /output/wordcount/part-00001
```

**Sample Output from part-00000:**
```
(Hello,3)
(World,2)
(Spark,6)
(Apache,2)
(is,1)
(amazing,1)
```

**Sample Output from part-00001:**
```
(runs,1)
(on,1)
(Hadoop,2)
(YARN,2)
(Big,2)
(Data,2)
(processing,1)
```

### Step 3: View Complete Results

```bash
# View all output parts together
hadoop fs -cat /output/wordcount/part-*
```

**Complete Output:**
```
(Hello,3)
(World,2)
(Spark,6)
(Apache,2)
(is,1)
(amazing,1)
(runs,1)
(on,1)
(Hadoop,2)
(YARN,2)
(Big,2)
(Data,2)
(processing,1)
(with,1)
(HDFS,1)
(stores,1)
(data,1)
(manages,1)
(cluster,1)
(resources,1)
(Distributed,1)
(computing,1)
```

### Step 4: Compare with Input

```bash
# View original input file
hadoop fs -cat /input/words.txt

# Compare line by line
echo "=== INPUT ==="
hadoop fs -cat /input/words.txt
echo -e "\n=== OUTPUT ==="
hadoop fs -cat /output/wordcount/part-*
```

### Step 5: Download Results to Local

```bash
# Download entire output directory
hadoop fs -get /output/wordcount /home/danny/wordcount_results

# View locally
cat /home/danny/wordcount_results/part-*

# Or view with more details
ls -la /home/danny/wordcount_results/
cat /home/danny/wordcount_results/part-00000
cat /home/danny/wordcount_results/part-00001
```

### Step 6: Verify Using Alternative Commands

```bash
# Using 'hdfs dfs' instead of 'hadoop fs'
hdfs dfs -ls /output/wordcount
hdfs dfs -cat /output/wordcount/part-*

# Check file sizes
hdfs dfs -du -h /output/wordcount

# Count number of lines in output
hdfs dfs -cat /output/wordcount/part-* | wc -l
```

---

## 📖 Understanding the Spark Submit Command

Let's break down each component of the `spark-submit` command:

```bash
bin/spark-submit \
  --class com.example.Spark_Submit_Demo.WordCount \
  --master yarn \
  --deploy-mode cluster \
  --num-executors 1 \
  /home/danny/spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
  hdfs://localhost:50000/input/words.txt \
  hdfs://localhost:50000/output/wordcount
```

### Command Breakdown

| Parameter | Value | Description |
|-----------|-------|-------------|
| `bin/spark-submit` | - | Spark job submission script located in `$SPARK_HOME/bin/` |
| `--class` | `com.example.Spark_Submit_Demo.WordCount` | **Fully qualified main class name** containing the `main()` method to execute |
| `--master` | `yarn` | **Cluster manager**: `yarn` (Hadoop YARN), `local[*]` (local mode), `spark://HOST:PORT` (standalone) |
| `--deploy-mode` | `cluster` | **Driver location**: `cluster` (runs on YARN), `client` (runs on submit machine) |
| `--num-executors` | `1` | **Number of executor processes** to launch for this application |
| JAR path | `/home/danny/spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar` | **Application JAR file** with all dependencies included |
| Input path | `hdfs://localhost:50000/input/words.txt` | **First application argument**: HDFS path to input file |
| Output path | `hdfs://localhost:50000/output/wordcount` | **Second application argument**: HDFS path for output (must not exist) |

### Additional Optional Parameters

```bash
bin/spark-submit \
  --class com.example.Spark_Submit_Demo.WordCount \
  --master yarn \
  --deploy-mode cluster \
  --driver-memory 2g \              # Memory for driver process (default: 1g)
  --executor-memory 2g \            # Memory per executor (default: 1g)
  --executor-cores 2 \              # CPU cores per executor (default: 1)
  --num-executors 2 \               # Number of executors (default: 2)
  --conf spark.yarn.maxAppAttempts=2 \  # Max application attempts
  --conf spark.executor.memoryOverhead=512m \  # Overhead memory
  /home/danny/spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
  hdfs://localhost:50000/input/words.txt \
  hdfs://localhost:50000/output/wordcount
```

### Deploy Mode Comparison

| Feature | Client Mode | Cluster Mode |
|---------|-------------|--------------|
| Driver Location | Runs on submit machine | Runs on YARN cluster |
| Console Output | Visible in terminal | Check YARN logs |
| Use Case | Development, debugging | Production, scheduled jobs |
| Network | Driver on local network | Driver on cluster network |
| Command | `--deploy-mode client` | `--deploy-mode cluster` |

### Resource Allocation Guidelines

| Dataset Size | Executors | Executor Memory | Executor Cores | Driver Memory |
|--------------|-----------|-----------------|----------------|---------------|
| < 100 MB | 1-2 | 1g | 1 | 1g |
| 100 MB - 1 GB | 2-4 | 2g | 2 | 2g |
| 1 GB - 10 GB | 4-8 | 4g | 2-4 | 2g-4g |
| > 10 GB | 8-16 | 4g-8g | 4 | 4g-8g |

### HDFS URI Formats

Both formats work with proper Hadoop configuration:

```bash
# Full URI format (explicit)
hdfs://localhost:50000/input/words.txt

# Short format (uses fs.defaultFS from core-site.xml)
/input/words.txt
```

---

## 🔄 Re-running the Application

### If Output Already Exists

```bash
# Remove existing output directory
hadoop fs -rm -r /output/wordcount

# Re-run spark-submit
cd ~/Downloads/spark-3.5.7-bin-hadoop3
bin/spark-submit \
  --class com.example.Spark_Submit_Demo.WordCount \
  --master yarn \
  --deploy-mode cluster \
  --num-executors 1 \
  /home/danny/spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
  hdfs://localhost:50000/input/words.txt \
  hdfs://localhost:50000/output/wordcount
```

### Clean Up All Output

```bash
# Remove all output directories
hadoop fs -rm -r /output/*

# Or remove specific output
hadoop fs -rm -r /output/wordcount
```

---

## 🛠️ Troubleshooting

### Issue 1: HADOOP_CONF_DIR Not Set

**Error:**
```
When running with master 'yarn' either HADOOP_CONF_DIR or YARN_CONF_DIR must be set
```

**Solution:**
```bash
# Add to ~/.bashrc
export HADOOP_CONF_DIR=$HADOOP_HOME/etc/hadoop
export YARN_CONF_DIR=$HADOOP_HOME/etc/hadoop

# Reload
source ~/.bashrc
```

---

### Issue 2: Output Directory Already Exists

**Error:**
```
org.apache.hadoop.mapred.FileAlreadyExistsException: Output directory already exists
```

**Solution:**
```bash
hadoop fs -rm -r /output/wordcount
```

---

### Issue 3: Input File Not Found

**Error:**
```
java.io.FileNotFoundException: File does not exist: hdfs://localhost:50000/input/words.txt
```

**Solution:**
```bash
# Check if file exists
hadoop fs -ls /input/

# Upload if missing
hdfs dfs -put /home/danny/words.txt hdfs://localhost:50000/input/words.txt
```

---

### Issue 4: HDFS Not Running

**Error:**
```
java.net.ConnectException: Connection refused
```

**Solution:**
```bash
# Check if HDFS is running
jps | grep NameNode

# Start HDFS if not running
start-dfs.sh

# Verify
jps
```

---

### Issue 5: YARN Not Running

**Error:**
```
Application application_xxx failed 2 times
```

**Solution:**
```bash
# Check if YARN is running
jps | grep ResourceManager

# Start YARN if not running
start-yarn.sh

# Verify
jps
```

---

### Issue 6: Missing Application Arguments

**Error:**
```
Usage: WordCount <input_path> <output_path>
exitCode: 13
```

**Solution:**
Make sure you provide both input and output paths:
```bash
bin/spark-submit \
  --class com.example.Spark_Submit_Demo.WordCount \
  --master yarn \
  --deploy-mode cluster \
  --num-executors 1 \
  /home/danny/spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
  hdfs://localhost:50000/input/words.txt \      # ✅ Input path
  hdfs://localhost:50000/output/wordcount       # ✅ Output path
```

---

### Issue 7: Permission Denied

**Error:**
```
org.apache.hadoop.security.AccessControlException: Permission denied
```

**Solution:**
```bash
# Check HDFS permissions
hadoop fs -ls -d /input
hadoop fs -ls -d /output

# Fix permissions if needed
hadoop fs -chmod -R 755 /input
hadoop fs -chmod -R 755 /output
```

---

## 📁 Project Structure

### Complete Directory Structure

```
Spark_Submit_Demo/
├── pom.xml                                     # Maven build configuration
├── README.md                                   # This documentation
└── src/
    └── main/
        └── java/
            └── com/
                └── example/
                    └── Spark_Submit_Demo/
                        └── WordCount.java      # Main application class
```

### Key Files

#### WordCount.java
- **Package:** `com.example.Spark_Submit_Demo`
- **Main Class:** `WordCount`
- **Purpose:** Reads text from HDFS, counts words, writes results to HDFS

#### pom.xml
- **Group ID:** `com.spark.example`
- **Artifact ID:** `spark_example`
- **Version:** `0.0.1-SNAPSHOT`
- **Main Class:** `com.example.Spark_Submit_Demo.WordCount`
- **Dependencies:** Spark Core 2.11, Hadoop Client 2.7.3

---

## 📚 HDFS Command Reference

### Essential HDFS Commands

```bash
# List files
hadoop fs -ls <path>
hadoop fs -ls -R <path>                # Recursive

# Create directory
hadoop fs -mkdir <path>
hadoop fs -mkdir -p <path>             # Create parent directories

# Upload file
hadoop fs -put <local_path> <hdfs_path>
hadoop fs -copyFromLocal <local_path> <hdfs_path>

# Download file
hadoop fs -get <hdfs_path> <local_path>
hadoop fs -copyToLocal <hdfs_path> <local_path>

# View file content
hadoop fs -cat <path>
hadoop fs -tail <path>                 # Last 1KB
hadoop fs -head <path>                 # First 1KB

# Delete file/directory
hadoop fs -rm <path>
hadoop fs -rm -r <path>                # Recursive delete

# Copy within HDFS
hadoop fs -cp <source> <destination>

# Move within HDFS
hadoop fs -mv <source> <destination>

# Check disk usage
hadoop fs -du -h <path>
hadoop fs -df -h                       # Filesystem capacity

# File information
hadoop fs -stat <path>
hadoop fs -count <path>
```

### Alternative: Using 'hdfs dfs' Commands

```bash
# All 'hadoop fs' commands work with 'hdfs dfs'
hdfs dfs -ls /
hdfs dfs -cat /input/words.txt
hdfs dfs -put file.txt /input/
```

---

## 🌐 Web UI Access

### HDFS NameNode Web UI
- **URL:** `http://localhost:50070`
- **Features:**
  - Browse file system
  - View DataNode status
  - Check cluster health
  - Monitor storage usage

### YARN ResourceManager Web UI
- **URL:** `http://localhost:8088`
- **Features:**
  - View running applications
  - Check application logs
  - Monitor cluster resources
  - View completed jobs

### Spark History Server (Optional)
```bash
# Start Spark History Server
$SPARK_HOME/sbin/start-history-server.sh

# Access at
http://localhost:18080
```

---

## 🧪 Testing with Different Inputs

### Test 1: Small File

```bash
# Create small test file
echo -e "test\ntest\nword" > /home/danny/small.txt

# Upload to HDFS
hdfs dfs -put /home/danny/small.txt /input/small.txt

# Run Spark job
bin/spark-submit \
  --class com.example.Spark_Submit_Demo.WordCount \
  --master yarn \
  --deploy-mode cluster \
  --num-executors 1 \
  /home/danny/spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
  /input/small.txt \
  /output/small_result

# View results
hadoop fs -cat /output/small_result/part-*
```

### Test 2: Large File

```bash
# Create large test file (1000 lines)
for i in {1..1000}; do 
  echo "Hello World Spark Hadoop Big Data Processing Line $i"
done > /home/danny/large.txt

# Upload to HDFS
hdfs dfs -put /home/danny/large.txt /input/large.txt

# Run with more resources
bin/spark-submit \
  --class com.example.Spark_Submit_Demo.WordCount \
  --master yarn \
  --deploy-mode cluster \
  --driver-memory 2g \
  --executor-memory 2g \
  --executor-cores 2 \
  --num-executors 2 \
  /home/danny/spark_example-0.0.1-SNAPSHOT-jar-with-dependencies.jar \
  /input/large.txt \
  /output/large_result

# View results
hadoop fs -cat /output/large_result/part-* | head -20
```

---

## 📊 Performance Monitoring

### Monitor Resource Usage

```bash
# YARN application list
yarn application -list

# View specific application
yarn application -status <application_id>

# View application logs
yarn logs -applicationId <application_id>

# Kill running application
yarn application -kill <application_id>
```

### Check Cluster Resources

```bash
# Node status
yarn node -list
yarn node -status <node_id>

# Queue information
yarn queue -status default
```

---

## 🎓 Learning Resources

- [Apache Spark Documentation](https://spark.apache.org/docs/latest/)
- [Hadoop Documentation](https://hadoop.apache.org/docs/stable/)
- [YARN Architecture](https://hadoop.apache.org/docs/stable/hadoop-yarn/hadoop-yarn-site/YARN.html)
- [HDFS Architecture](https://hadoop.apache.org/docs/stable/hadoop-project-dist/hadoop-hdfs/HdfsDesign.html)

---

## 🤝 Contributing

Contributions are welcome! Please feel free to submit pull requests or open issues.

---

## 📝 Summary Checklist

- [x] Build JAR on MacBook using Maven
- [x] Transfer JAR to Ubuntu VM
- [x] Configure environment variables
- [x] Start HDFS services
- [x] Start YARN services
- [x] Create HDFS input directory
- [x] Upload input file to HDFS
- [x] Submit Spark job to YARN
- [x] Monitor job execution
- [x] View results in HDFS
- [x] Download results to local

---


**Happy Spark Programming! 🚀**

*Last Updated: November 2025*  
*Platform: Ubuntu VMware VM with Hadoop 2.9.0 & Spark 3.5.7*

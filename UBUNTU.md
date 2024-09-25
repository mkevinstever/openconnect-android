### 1. Update and install required packages

Run the following commands in the terminal to update the package list and install the necessary dependencies:

```bash
sudo apt-get update
sudo apt-get install -y git autoconf automake libtool build-essential gcc make
```

### 2. Install JDK 8

You can use either **OpenJDK** or **Temurin** versions. Here are the commands to install OpenJDK 8:

```bash
sudo apt-get install -y openjdk-8-jdk
```

If you need to set the JAVA_HOME environment variable, you can add it in `~/.bashrc` or `~/.bash_profile`:

```bash
export JAVA_HOME=/usr/lib/jvm/java-8-openjdk-amd64
export PATH=$JAVA_HOME/bin:$PATH
```

Run `source ~/.bashrc` to make the changes take effect.

### 3. Download and install Android SDK

Run the following commands to create a directory and download Android SDK tools:

```bash
mkdir -p $HOME/android-sdk
cd $HOME/android-sdk
wget https://dl.google.com/android/repository/sdk-tools-linux-3859397.zip
unzip sdk-tools-linux-3859397.zip
rm sdk-tools-linux-3859397.zip
```

### 4. Set environment variables

Add the following to `~/.bashrc`:

```bash
export ANDROID_HOME=$HOME/android-sdk
export PATH=$ANDROID_HOME/tools:$ANDROID_HOME/platform-tools:$ANDROID_HOME/tools/bin:$PATH
```

Run `source ~/.bashrc` to make the changes take effect.

### 5. Install Android SDK components

Use the following commands to accept the license and install the required SDK components:

```bash
yes | $ANDROID_HOME/tools/bin/sdkmanager --sdk_root=$ANDROID_HOME --licenses
$ANDROID_HOME/tools/bin/sdkmanager --sdk_root=$ANDROID_HOME "platform-tools" "platforms;android-23"
```

### 6. Download and install NDK r25c

```bash
cd $HOME
wget https://dl.google.com/android/repository/android-ndk-r25c-linux.zip
unzip android-ndk-r25c-linux.zip -d /opt/android-sdk-linux_x86/
rm android-ndk-r25c-linux.zip
```

### 7. Install Apache Ant

```bash
wget https://downloads.apache.org/ant/binaries/apache-ant-1.10.14-bin.tar.bz2
tar -xvjf apache-ant-1.10.14-bin.tar.bz2 -C /opt/
echo "/opt/apache-ant-1.10.14/bin" >> ~/.bashrc
source ~/.bashrc
rm apache-ant-1.10.14-bin.tar.bz2
```

### 8. Give gradlew executable permissions

If you have a `gradlew` file in your project, make sure it has executable permissions:

```bash
chmod +x gradlew
```

### 9. Initialize and update Git submodules

```bash
git submodule init
git submodule update
```

### 10. Build external dependencies

Go to the `external` directory, set the path to the NDK and run `make`:

```bash
cd external
export PATH=/opt/android-sdk-linux_x86/android-ndk-r25c/toolchains/llvm/prebuilt/linux-x86_64/bin:$PATH
export NDK=/opt/android-sdk-linux_x86/android-ndk-r25c
make VERBOSE=1
```

### 11. Build APK

Finally, run the Gradle build command in the project root directory:

```bash
./gradlew build
```

### 12. Output APK

After the build is complete, you can find the generated APK file in the `app/build/outputs/apk` directory.


--------------------------------------------------------------------


If it says `libnettle 3.6` is missing, you can of course compile and install `libnettle 3.6`.

### Step 1: Install compilation dependencies

Before compiling `libnettle`, you need to make sure that some necessary tools and libraries are installed on your system. Open a terminal and run the following commands:

```bash
sudo apt-get update
sudo apt-get install -y build-essential
```

### Step 2: Download source code

Get the source code of `libnettle 3.6` from the official website or GitHub. You can use the `wget` command to download:

```bash
wget https://ftp.gnu.org/gnu/nettle/nettle-3.6.tar.gz
```

### Step 3: Extract the source code

Extract the downloaded tar file:

```bash
tar -xvzf nettle-3.6.tar.gz
cd nettle-3.6
```

### Step 4: Configure the compilation environment

Before compiling, you need to configure the installation path and other options. Run the following command:

```bash
./configure --prefix=/usr/local
```

This will install `libnettle` to `/usr/local`.

### Step 5: Compile and Install

Next, compile and install `libnettle`:

```bash
make
sudo make install
```

### Step 6: Verify Installation

You can verify that `libnettle` has been successfully installed using the following command:

```bash
ldconfig -p | grep nettle
```

If the installation was successful, you should see output related to `libnettle`.

### Other Notes

- **Permission Issues**: If you encounter permission issues when `make install`, make sure to use `sudo`.

- **Dynamic Link Library Path**: If you get errors that the library cannot be found when using the library, you may need to add `/usr/local/lib` to `/etc/ld.so.conf` and run `sudo ldconfig` to update the dynamic link library cache.

# Setup 

* Make sure java 8 jdk is installed.
* Fetch the selenium chromedriver from http://chromedriver.storage.googleapis.com/index.htm and place it in "C:\Program Files (x86)\selenium\chromedriver.exe"
* run `wahrnehmung_dev.launch` 

# Deploy

gcloud config configurations list
gcloud config configurations activate [configuration]
gcloud app deploy -v [version] [build folder in target e.g. target/notice-23-2]

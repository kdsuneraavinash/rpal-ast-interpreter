cd src
mkdir -p ../bin
javac Rpal.java -d ../bin
cd ../bin
jar cfve rpal.jar Rpal Rpal.class cse tree
mv rpal.jar ..

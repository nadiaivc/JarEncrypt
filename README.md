# JarEncrypt
JAR encryption and dynamic loading of classes when entering the correct password
#### It's just for explore of one of the methods of protecting the program (I encrypted my mini game)
The useful part of the program that needs to be hidden is packaged in a JAR and encrypted with AES-GCM encryption. (ClassEncryption.java). 
1. The password is hashed using SHA256 - this will be our key.
2. We generate nonce
3. Initialize the cipher ([1], [2])
4. Encrypt (nonce will be at the beginning of the file)
#### And to decrypt the JAR, you must enter the correct password, there are no other options

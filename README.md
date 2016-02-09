**************************************************** WELCOME TO DISTRIBUTED CAN ***************************************************************

Author - Vaibhav Page

NOTE : When a node requests for a file in CAN, the program doesnt print IP
       of node where file is stored in requesting node's terminal . Rather I did sysout in terminal of node where the file is actually present.
       So if you do search , you will directly get file from node where the file is stored , but sender's IP wont be displayed , instead
       in sender's terminal you will see the IP of the node who requested that file. Thank you.


NOTE : I also have added .class files and stub class files in my submission. 

This project demonstrates Distributed Content Addressable Network Implementation.
Project is developed in Java.

Main Interfaces - 
1) CANNodeService - It exposes the methods that remote object of Peer/Node has.
2) BootsrapService - Interface to expose methods of Bootstrap Server
3) FileTransInterface - Interface for File Server.


Naming - 
1) BootsrapService(BootstrapServiceImpl) is bound in rmiregistry by the name "BootstrapServer"
2) CANNodeService(CANNodeServiceImpl) object is bound by the name same as their IP (Node has a name which is its IP)
3) FileTransInterface(FileTransImpl) remote object bound by name its IP + "FileTransferObject"


To RUN the project - 

1) Compile all java files. 

2) Run Bootstrap 
   i) rmic BootstrapServiceImpl.java
   ii) Start rmiregistry in different terminal
   iii) run BootstrapServiceImpl
   iv) You can also check the remote objects bound to rmiregistry by running Inet.java file(It displays IP of machine + remote objects)

3) Run CANNode/Peer - 
   i) rmic CANNodeServiceImpl
   ii) rmic FileTransImpl
   iii) start rmiregistry
   iv) run FileTransImpl in seperate terminal
   v) run CANNodeServiceImpl in seperate terminal


To select different options in displayed menu in CANNodeServiceImpl - 
	
   Node Join -
   i) When CANNodeServiceImpl is run , it asks Bootstrap IP. 
      Please Enter Bootstrap's IP correctly.
      joins CAN and displays it's own information with its neighbors if any
   
    Menu 1 : File Store -
   i) Enter the name of file that you already have in the folder from where you are running FileTransImpl

   2 : File Search - 
   i) Enter name of the file that you want to search. 
      

   3 : Leave - 
   i) When Node wants to leave . Displays the Node IP to which control is given.

   NOTE : this implementation does not handle any kind of odd shaped zone.
      

   4 : Print Information -
   i) Prints the information about node's name , its zone boundary, name of files it has, files coordinates, its neighbors name, their   
      zone boundaries



                                                   ****THANK YOU****


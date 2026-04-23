package Wilson;
import java.util.*;
import java.io.*;
import java.security.MessageDigest;
import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;


public class PasswordManager{
    static Scanner scanner=new Scanner(System.in);
    static final String FILE_NAME="Passwords.txt";
    public static void main(String[]args){

        System.out.print("Enter Master Password: ");
        String masterPassword=scanner.next();
        while(true){
            
            System.out.println("\n1.Add Password ");
            System.out.println("2.View Passwords");
            System.out.println("3.Change Password ");
            System.out.println("4.Delete Password ");
            System.out.println("5.Exit ");

            int choice =scanner.nextInt();
            switch (choice) {
                case 1:
                    addPassword(masterPassword);
                    break;
                case 2:
                    viewPasswords(masterPassword);
                    break;
                case 3:
                    changePassword(masterPassword);
                    break;
                case 4:
                    deletePassword();
                    break;
                case 5:
                    System.out.println("...Exiting");
                    return;
            }

        }
    }
    static void addPassword(String masterPassword){
        try{
        System.out.print("Site: ");
        String site=scanner.next();

        System.out.print("Username: ");
        String username=scanner.next();

        System.out.print("Password: ");
        String password=scanner.next();


        FileWriter fw=new FileWriter(FILE_NAME,true);
        String encryptedPassword=encrypt(password, masterPassword);
        fw.write(site+","+username+","+encryptedPassword+"\n");
        fw.close();
        System.out.println("Success.Password saved.");
        }
        catch(IOException e){
            System.out.println("Error saving your password!");
        }
    }    
    static void viewPasswords(String masterPassword){
        try {
        File file=new File(FILE_NAME);
        if(!file.exists()){
            System.out.println("No passwords saved yet.");
            return;
        }

        Scanner fileScanner=new Scanner(file);
        while (fileScanner.hasNextLine()){
            String line =fileScanner.nextLine();
            String[] parts=line.split(",");
            String decrypted=decrypt(parts[2],masterPassword);
            System.out.println("Site: "+parts[0]+
                                    " |User: "+parts[1]+
                                    " |Password: "+decrypted);
        }
        fileScanner.close();

        }
        catch (Exception e){
            System.out.println("Error reading passwords.");
        }

    }

    static List<String[]> loadPasswords(){
        List<String[]> list=new ArrayList<>();

        try{
            File file=new File(FILE_NAME);
            if(!file.exists())return list;
            Scanner sc=new Scanner(file);

            while(sc.hasNextLine()){
                String[] parts=sc.nextLine().split(",");
                list.add(parts);
            }
            sc.close();
        }
        catch(Exception e){
            System.out.println("Error loading the data");

        }
        return list;

    }
    
    static void saveAll(List<String[]> list){
        try{
            FileWriter fw=new FileWriter(FILE_NAME);
            for(String[] entry:list){
                fw.write(String.join(",",entry)+"\n");
            }
            fw.close();
        }
        catch(IOException e){
            System.out.println("Error Saving data");
        }

    }
    
    
    static void changePassword(String masterPassword){
        List<String[]> list=loadPasswords();
        if(list.isEmpty()){
            System.out.println("No data to edit");
            return;
        }
        for(int i=0;i<list.size();i++){
            System.out.println(i+": "+list.get(i)[0]+ "(" +list.get(i)[1]+")" );
        }
        System.out.print("Select index to change: ");
        int index=scanner.nextInt();

        if (index<0||index>=list.size()){
            System.out.println("Invalid index: ");
            return;
        }
        
        System.out.print("New password: ");
        String newPass=scanner.next();
        String encrypted=encrypt(newPass, masterPassword);
        list.get(index)[2]=encrypted;

        saveAll(list);
        System.out.println("Your password has been updated successfully");


    }
    static void deletePassword(){
        List<String[]> list=loadPasswords();
        if(list.isEmpty()){System.out.println("No data to delete");
        return;}
        
        for (int i=0;i<list.size();i++){
            System.out.println(i+": "+list.get(i)[0]+" (" +list.get(i)[1]+")");
        }
        System.out.println("Enter the index you want to delete: ");
        int index=scanner.nextInt();

        if(index==0||index>=list.size()){
            System.out.println("Enter a valid index ");
        }
        list.remove(index);
        saveAll(list);
        System.out.println("Deleted successfully.");

    }
    static SecretKeySpec getKey(String masterPassword){
        try{
            byte[] key =masterPassword.getBytes("UTF-8");
            MessageDigest sha=MessageDigest.getInstance("SHA-256");
            key=sha.digest(key);
            key=Arrays.copyOf(key, 16);

            return new SecretKeySpec(key,"AES");

        }

        catch(Exception e){
            throw new RuntimeException(e);
        }

    }

    static String encrypt(String data,String masterPassword){

        try{
            SecretKeySpec key =getKey(masterPassword);
            Cipher cipher=Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE,key);
            byte[] encrypted=cipher.doFinal(data.getBytes());
            
            return Base64.getEncoder().encodeToString(encrypted);


        }

        catch(Exception e){
            throw new RuntimeException(e);
        }
    }    

    static String decrypt(String data,String masterPassword){
        try{
            SecretKeySpec key=getKey(masterPassword);
            Cipher cipher=Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE,key);
            byte[] decoded=Base64.getDecoder().decode(data);

            return new String(cipher.doFinal(decoded));

        }

        catch(Exception e){

            return "Wrong Master password";
        }
    }

}

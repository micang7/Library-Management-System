package user;

import java.io.*;

public class Admin implements Serializable {
    protected String password;

    public Admin() {}

    public String getPassword() {
        return password;
    }

    public void changePassword(String password) {
        this.password = password;
    }

    final public boolean verifyPassword(String password) {
        return this.password.compareTo(password) == 0;
    }

    public boolean serialize(String file) {
        try {
            FileOutputStream fos = new FileOutputStream(file);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.flush();
            oos.close();
            return true;
        } catch (Exception _) {
            return false;
        }
    }

    public Admin deserialize(String file) {
        try {
            FileInputStream fis = new FileInputStream(file);
            ObjectInputStream ois = new ObjectInputStream(fis);
            Admin deserialized = (Admin) ois.readObject();
            ois.close();
            return deserialized;
        } catch (Exception _) {
            return null;
        }
    }

    public String toString() {
        return "Password: " + password + "\n";
    }
}
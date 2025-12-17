package utils;

public class Session {
    private static int userId;
    private static String email;
    private static String role;

    public static void setUser(int id, String mail, String r) {
        userId = id;
        email = mail;
        role = r;
    }

    public static int getUserId() { return userId; }
    public static String getEmail() { return email; }
    public static String getRole() { return role; }

    public static void clear() {
        userId = 0;
        email = null;
        role = null;
    }
}

package nl.hdkesting.javatwitter.accounts.support;

public class ConnStr {
    private ConnStr() {}

    public static String H2(String datapath) {
        return "jdbc:h2:mem:accountdb;" +
                "INIT=RUNSCRIPT FROM 'classpath:create_account.sql'\\;" +
                "RUNSCRIPT FROM 'classpath:" + datapath + "'";
    }

    public static String H2() {
        return H2("data_account.sql");
    }
}


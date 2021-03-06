package th.pd.mail.dao;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * the account associated with a mail address
 */
public class MailAcc {

    private String addr;
    private String caption;

    public MailAcc(String addr, String caption) {
        this.addr = addr;
        this.caption = caption;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        } else if (o instanceof MailAcc) {
            final MailAcc other = (MailAcc) o;
            return this.addr.equals(other.addr);
        }
        return false;
    }

    public String getAddr() {
        return this.addr;
    }

    public String getCaption() {
        return this.caption;
    }

    private String getDefaultCaption() {
        if (this.addr != null) {
            Pattern pattern = Pattern.compile(".*@(.*)",
                    Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(getAddr());
            if (matcher.matches()) {
                return matcher.group(1);
            }
        }
        return null;
    }

    public String getFriendlyCaption() {
        return caption == null || caption.isEmpty()
                ? getDefaultCaption()
                : caption;
    }
}

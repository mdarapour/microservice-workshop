package au.com.gumtree.ms.workshop.domain;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;

/**
 * @author mdarapour
 */
@Entity
public class User {
    @Id
    @GeneratedValue
    private Long   id;
    @Column(nullable = false)
    private String mail;
    @Column
    private Long   messageCount;

    protected User() {
    }

    public User(String mail) {
        this.mail = mail;
    }

    public Long getId() {
        return id;
    }

    public String getMail() {
        return mail;
    }

    public Long getMessageCount() {
        return messageCount;
    }

    public User setMessageCount(Long messageCount) {
        this.messageCount = messageCount;
        return this;
    }

    @Override
    public String toString() {
        return "User{" +
                "id=" + id +
                ", name='" + mail + '\'' +
                ", messageCount=" + messageCount +
                '}';
    }
}

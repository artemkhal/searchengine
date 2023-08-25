package searchengine.model;

import lombok.*;

import javax.persistence.*;
import javax.persistence.Index;

@Entity
@Table
        (name = "`page`"
                , indexes = @Index(name = "`path_index`",columnList = "path")
                , uniqueConstraints = { @UniqueConstraint(columnNames = {"path", "site_id"}
        )}
        )
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder(builderMethodName = "pageBuilder")
public class Page {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "path", nullable = false, columnDefinition = "VARCHAR(255)") //изменил TEXT на VARCHAR т.к. с типом TEXT нет возможности установить индекс(из-за неопределенной длинны)
    private String path;

    @Column(name = "code", nullable = false, columnDefinition = "INT")
    private int code;

    @Column(name = "content", columnDefinition = "MEDIUMTEXT")//убрал параметр nullable = false т.к. при возврате статуса >= 400 контент отсутствует(content = null)
    private String content;

    @ManyToOne
    @JoinColumn(name = "site_id", nullable = false, columnDefinition = "INT" )
    private Site site;

    @Override
    public String toString() {
        return "Page{" +
                "id=" + id +
                ", path='" + path + '\'' +
                '}';
    }
}

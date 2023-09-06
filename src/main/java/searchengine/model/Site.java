package searchengine.model;

import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "site")
@Getter
@Setter
@Builder(builderMethodName = "siteBuilder")
public class Site {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;

    @Column(name = "status", nullable = false, columnDefinition = "ENUM('INDEXING','INDEXED','FAILED')")
    @Enumerated(value = EnumType.STRING)
    private Status status;

    @Column(name = "status_time", nullable = false, columnDefinition = "DATETIME")
    private LocalDateTime statusTime;

    @Column(name = "last_error", columnDefinition = "TEXT")
    private String lastErr;

    @Column(name = "`url`", nullable = false, columnDefinition = "VARCHAR(255)")
    private String url;

    @Column(name = "`name`", nullable = false, columnDefinition = "VARCHAR(255)")
    private String name;

    @OneToMany(mappedBy = "site")
    private List<Page> pages;

}

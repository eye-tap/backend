package ch.ethz.eyetap.model.annotation;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.LinkedHashSet;
import java.util.Set;

@Getter
@Setter
@Entity
@Table(name = "data_set")
public class DataSet {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id", nullable = false)
    private Long id;

    @OneToMany(mappedBy = "dataSet", orphanRemoval = true)
    private Set<Text> texts = new LinkedHashSet<>();

    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "description")
    private String description;

}
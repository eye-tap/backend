package ch.ethz.eyetap.model.annotation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.*;

@Getter
@Setter
@Embeddable
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BoundingBox {
    @Column(name = "x_min", nullable = false)
    private Double xMin;

    @Column(name = "x_max", nullable = false)
    private Double xMax;

    @Column(name = "y_min", nullable = false)
    private Double yMin;

    @Column(name = "y_max", nullable = false)
    private Double yMax;


    public boolean contains(Double px, Double py) {
        return px >= xMin && px <= xMax &&
                py >= yMin && py <= yMax;
    }

}
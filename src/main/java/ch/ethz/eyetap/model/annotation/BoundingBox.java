package ch.ethz.eyetap.model.annotation;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Embeddable
public class BoundingBox {
  @Column(name = "x_min", nullable = false)
  private Long xMin;

  @Column(name = "x_max", nullable = false)
  private Long xMax;

  @Column(name = "y_min", nullable = false)
  private Long yMin;

  @Column(name = "y_max", nullable = false)
  private Long yMax;


}
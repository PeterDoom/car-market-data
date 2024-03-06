package io.demo.cars.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import java.util.Set;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Entity
@Table(name = "models")
public class CarModel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    private String brandName;

    @ManyToOne(cascade = CascadeType.MERGE, fetch = FetchType.EAGER)
    private CarBrand brand;

    @OneToMany(mappedBy = "carModel", fetch = FetchType.EAGER, cascade = CascadeType.ALL)
    private Set<RawCarAdvert> rawCarAdverts;
}

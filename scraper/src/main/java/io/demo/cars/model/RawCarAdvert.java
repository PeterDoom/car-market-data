package io.demo.cars.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.Hibernate;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import java.util.Objects;

@Builder
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "raw_car_adverts")
public class RawCarAdvert {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    protected Long id;

    private String advertLink;

    private String advertNumber;

    @ManyToOne
    private CarModel carModel;

    private String year;

    private String price;

    private String currency;

    private String visitCount;

    @Column(name = "last_updated_by_seller")
    private String lastUpdatedBySeller;

    @Column(name = "scraped_at")
    private Long scrapedAt;

    @Column(name = "engine_info")
    private String engineInfo;

    private String horsepower;

    private String mileage;

    private String gearboxType;

    private String cubicCapacity;

    private String color;

    private String location;

    @Column(name = "extras", columnDefinition = "TEXT")
    private String extras;

    @Column(name = "description_from_add", columnDefinition = "TEXT")
    private String descriptionFromStart;

    @Column(name = "coupe_type")
    private String coupeType;

    @Column(name = "seller_type")
    private String sellerType;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) return false;
        RawCarAdvert that = (RawCarAdvert) o;
        return id != null && Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }

}

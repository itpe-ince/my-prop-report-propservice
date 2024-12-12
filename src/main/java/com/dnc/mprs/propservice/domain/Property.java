package com.dnc.mprs.propservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A Property.
 */
@Entity
@Table(name = "property")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "property")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Property implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "complex_id", nullable = false)
    private Long complexId;

    @NotNull
    @Size(max = 255)
    @Column(name = "address", length = 255, nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String address;

    @Size(max = 255)
    @Column(name = "region_cd", length = 255)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String regionCd;

    @Size(max = 255)
    @Column(name = "local_name", length = 255)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String localName;

    @Size(max = 255)
    @Column(name = "street", length = 255)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String street;

    @Column(name = "floor")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Integer)
    private Integer floor;

    @NotNull
    @Size(max = 100)
    @Column(name = "type", length = 100, nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String type;

    @NotNull
    @Column(name = "area", precision = 21, scale = 2, nullable = false)
    private BigDecimal area;

    @NotNull
    @Column(name = "rooms", nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Integer)
    private Integer rooms;

    @NotNull
    @Column(name = "bathrooms", nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Integer)
    private Integer bathrooms;

    @NotNull
    @Column(name = "build_year", nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Integer)
    private Integer buildYear;

    @Size(min = 1, max = 1)
    @Column(name = "parking_yn", length = 1)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String parkingYn;

    @Column(name = "description")
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String description;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    private Complex complex;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Property id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getComplexId() {
        return this.complexId;
    }

    public Property complexId(Long complexId) {
        this.setComplexId(complexId);
        return this;
    }

    public void setComplexId(Long complexId) {
        this.complexId = complexId;
    }

    public String getAddress() {
        return this.address;
    }

    public Property address(String address) {
        this.setAddress(address);
        return this;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getRegionCd() {
        return this.regionCd;
    }

    public Property regionCd(String regionCd) {
        this.setRegionCd(regionCd);
        return this;
    }

    public void setRegionCd(String regionCd) {
        this.regionCd = regionCd;
    }

    public String getLocalName() {
        return this.localName;
    }

    public Property localName(String localName) {
        this.setLocalName(localName);
        return this;
    }

    public void setLocalName(String localName) {
        this.localName = localName;
    }

    public String getStreet() {
        return this.street;
    }

    public Property street(String street) {
        this.setStreet(street);
        return this;
    }

    public void setStreet(String street) {
        this.street = street;
    }

    public Integer getFloor() {
        return this.floor;
    }

    public Property floor(Integer floor) {
        this.setFloor(floor);
        return this;
    }

    public void setFloor(Integer floor) {
        this.floor = floor;
    }

    public String getType() {
        return this.type;
    }

    public Property type(String type) {
        this.setType(type);
        return this;
    }

    public void setType(String type) {
        this.type = type;
    }

    public BigDecimal getArea() {
        return this.area;
    }

    public Property area(BigDecimal area) {
        this.setArea(area);
        return this;
    }

    public void setArea(BigDecimal area) {
        this.area = area;
    }

    public Integer getRooms() {
        return this.rooms;
    }

    public Property rooms(Integer rooms) {
        this.setRooms(rooms);
        return this;
    }

    public void setRooms(Integer rooms) {
        this.rooms = rooms;
    }

    public Integer getBathrooms() {
        return this.bathrooms;
    }

    public Property bathrooms(Integer bathrooms) {
        this.setBathrooms(bathrooms);
        return this;
    }

    public void setBathrooms(Integer bathrooms) {
        this.bathrooms = bathrooms;
    }

    public Integer getBuildYear() {
        return this.buildYear;
    }

    public Property buildYear(Integer buildYear) {
        this.setBuildYear(buildYear);
        return this;
    }

    public void setBuildYear(Integer buildYear) {
        this.buildYear = buildYear;
    }

    public String getParkingYn() {
        return this.parkingYn;
    }

    public Property parkingYn(String parkingYn) {
        this.setParkingYn(parkingYn);
        return this;
    }

    public void setParkingYn(String parkingYn) {
        this.parkingYn = parkingYn;
    }

    public String getDescription() {
        return this.description;
    }

    public Property description(String description) {
        this.setDescription(description);
        return this;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Property createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public Property updatedAt(Instant updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    public Complex getComplex() {
        return this.complex;
    }

    public void setComplex(Complex complex) {
        this.complex = complex;
    }

    public Property complex(Complex complex) {
        this.setComplex(complex);
        return this;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Property)) {
            return false;
        }
        return getId() != null && getId().equals(((Property) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Property{" +
            "id=" + getId() +
            ", complexId=" + getComplexId() +
            ", address='" + getAddress() + "'" +
            ", regionCd='" + getRegionCd() + "'" +
            ", localName='" + getLocalName() + "'" +
            ", street='" + getStreet() + "'" +
            ", floor=" + getFloor() +
            ", type='" + getType() + "'" +
            ", area=" + getArea() +
            ", rooms=" + getRooms() +
            ", bathrooms=" + getBathrooms() +
            ", buildYear=" + getBuildYear() +
            ", parkingYn='" + getParkingYn() + "'" +
            ", description='" + getDescription() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}

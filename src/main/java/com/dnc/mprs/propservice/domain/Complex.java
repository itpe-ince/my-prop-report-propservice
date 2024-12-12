package com.dnc.mprs.propservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;

/**
 * A Complex.
 */
@Entity
@Table(name = "complex")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "complex")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Complex implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Size(max = 255)
    @Column(name = "complex_name", length = 255, nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String complexName;

    @Size(max = 100)
    @Column(name = "state", length = 100)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String state;

    @Size(max = 100)
    @Column(name = "county", length = 100)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String county;

    @Size(max = 100)
    @Column(name = "city", length = 100)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String city;

    @Size(max = 100)
    @Column(name = "town", length = 100)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String town;

    @Size(max = 10)
    @Column(name = "address_code", length = 10)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String addressCode;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Complex id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getComplexName() {
        return this.complexName;
    }

    public Complex complexName(String complexName) {
        this.setComplexName(complexName);
        return this;
    }

    public void setComplexName(String complexName) {
        this.complexName = complexName;
    }

    public String getState() {
        return this.state;
    }

    public Complex state(String state) {
        this.setState(state);
        return this;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getCounty() {
        return this.county;
    }

    public Complex county(String county) {
        this.setCounty(county);
        return this;
    }

    public void setCounty(String county) {
        this.county = county;
    }

    public String getCity() {
        return this.city;
    }

    public Complex city(String city) {
        this.setCity(city);
        return this;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getTown() {
        return this.town;
    }

    public Complex town(String town) {
        this.setTown(town);
        return this;
    }

    public void setTown(String town) {
        this.town = town;
    }

    public String getAddressCode() {
        return this.addressCode;
    }

    public Complex addressCode(String addressCode) {
        this.setAddressCode(addressCode);
        return this;
    }

    public void setAddressCode(String addressCode) {
        this.addressCode = addressCode;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Complex createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public Complex updatedAt(Instant updatedAt) {
        this.setUpdatedAt(updatedAt);
        return this;
    }

    public void setUpdatedAt(Instant updatedAt) {
        this.updatedAt = updatedAt;
    }

    // jhipster-needle-entity-add-getters-setters - JHipster will add getters and setters here

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof Complex)) {
            return false;
        }
        return getId() != null && getId().equals(((Complex) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Complex{" +
            "id=" + getId() +
            ", complexName='" + getComplexName() + "'" +
            ", state='" + getState() + "'" +
            ", county='" + getCounty() + "'" +
            ", city='" + getCity() + "'" +
            ", town='" + getTown() + "'" +
            ", addressCode='" + getAddressCode() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}

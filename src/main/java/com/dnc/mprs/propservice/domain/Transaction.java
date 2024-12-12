package com.dnc.mprs.propservice.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.math.BigDecimal;
import java.time.Instant;

/**
 * A Transaction.
 */
@Entity
@Table(name = "transaction")
@org.springframework.data.elasticsearch.annotations.Document(indexName = "transaction")
@SuppressWarnings("common-java:DuplicatedBlocks")
public class Transaction implements Serializable {

    private static final long serialVersionUID = 1L;

    @NotNull
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false)
    private Long id;

    @NotNull
    @Column(name = "property_id", nullable = false)
    private Long propertyId;

    @NotNull
    @Size(max = 50)
    @Column(name = "transaction_type", length = 50, nullable = false)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String transactionType;

    @NotNull
    @Column(name = "price", precision = 21, scale = 2, nullable = false)
    private BigDecimal price;

    @NotNull
    @Column(name = "transaction_date", nullable = false)
    private Instant transactionDate;

    @Size(max = 100)
    @Column(name = "buyer", length = 100)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String buyer;

    @Size(max = 100)
    @Column(name = "seller", length = 100)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String seller;

    @Size(max = 100)
    @Column(name = "agent", length = 100)
    @org.springframework.data.elasticsearch.annotations.Field(type = org.springframework.data.elasticsearch.annotations.FieldType.Text)
    private String agent;

    @NotNull
    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // jhipster-needle-entity-add-field - JHipster will add fields here

    public Long getId() {
        return this.id;
    }

    public Transaction id(Long id) {
        this.setId(id);
        return this;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getPropertyId() {
        return this.propertyId;
    }

    public Transaction propertyId(Long propertyId) {
        this.setPropertyId(propertyId);
        return this;
    }

    public void setPropertyId(Long propertyId) {
        this.propertyId = propertyId;
    }

    public String getTransactionType() {
        return this.transactionType;
    }

    public Transaction transactionType(String transactionType) {
        this.setTransactionType(transactionType);
        return this;
    }

    public void setTransactionType(String transactionType) {
        this.transactionType = transactionType;
    }

    public BigDecimal getPrice() {
        return this.price;
    }

    public Transaction price(BigDecimal price) {
        this.setPrice(price);
        return this;
    }

    public void setPrice(BigDecimal price) {
        this.price = price;
    }

    public Instant getTransactionDate() {
        return this.transactionDate;
    }

    public Transaction transactionDate(Instant transactionDate) {
        this.setTransactionDate(transactionDate);
        return this;
    }

    public void setTransactionDate(Instant transactionDate) {
        this.transactionDate = transactionDate;
    }

    public String getBuyer() {
        return this.buyer;
    }

    public Transaction buyer(String buyer) {
        this.setBuyer(buyer);
        return this;
    }

    public void setBuyer(String buyer) {
        this.buyer = buyer;
    }

    public String getSeller() {
        return this.seller;
    }

    public Transaction seller(String seller) {
        this.setSeller(seller);
        return this;
    }

    public void setSeller(String seller) {
        this.seller = seller;
    }

    public String getAgent() {
        return this.agent;
    }

    public Transaction agent(String agent) {
        this.setAgent(agent);
        return this;
    }

    public void setAgent(String agent) {
        this.agent = agent;
    }

    public Instant getCreatedAt() {
        return this.createdAt;
    }

    public Transaction createdAt(Instant createdAt) {
        this.setCreatedAt(createdAt);
        return this;
    }

    public void setCreatedAt(Instant createdAt) {
        this.createdAt = createdAt;
    }

    public Instant getUpdatedAt() {
        return this.updatedAt;
    }

    public Transaction updatedAt(Instant updatedAt) {
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
        if (!(o instanceof Transaction)) {
            return false;
        }
        return getId() != null && getId().equals(((Transaction) o).getId());
    }

    @Override
    public int hashCode() {
        // see https://vladmihalcea.com/how-to-implement-equals-and-hashcode-using-the-jpa-entity-identifier/
        return getClass().hashCode();
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "Transaction{" +
            "id=" + getId() +
            ", propertyId=" + getPropertyId() +
            ", transactionType='" + getTransactionType() + "'" +
            ", price=" + getPrice() +
            ", transactionDate='" + getTransactionDate() + "'" +
            ", buyer='" + getBuyer() + "'" +
            ", seller='" + getSeller() + "'" +
            ", agent='" + getAgent() + "'" +
            ", createdAt='" + getCreatedAt() + "'" +
            ", updatedAt='" + getUpdatedAt() + "'" +
            "}";
    }
}

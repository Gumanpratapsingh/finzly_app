package com.guman.bbc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Data
@Table(name = "consumerid")
public class ConsumerID {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "consumer_id")
    private Integer consumerId;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "connection_id", nullable = false, unique = true)
    private String connectionId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

//    public String getConnectionId() {
//        return this.connectionId;
//    }
}


//for the connections, one user can have many connection
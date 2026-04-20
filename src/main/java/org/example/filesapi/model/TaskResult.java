package org.example.filesapi.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "task_results")
@Getter
@Setter
@NoArgsConstructor
public class TaskResult {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private UUID id;

    @Column(name = "file_path", length = 1000, nullable = false)
    private String filePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "task_id", nullable = false)
    private Task task;
}
   /*
    POST http://localhost:8082/apiFiles/find-files
    responose -> taksId (wygeneruj UUID)

    GET http://localhost:8082/apiFiles/find-files/status/3
    zamiast 3 będziemy szukać po UUID

    Path -> Tabela
    1 kolumna - Path D/...
    2 kolumna - UUID task

    Relacje bazodanowe

    */

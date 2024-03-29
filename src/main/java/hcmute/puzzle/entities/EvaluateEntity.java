package hcmute.puzzle.entities;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import javax.persistence.*;

@Getter
@Setter
@NoArgsConstructor
@Entity
@Table(name = "evaluate")
public class EvaluateEntity {

  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  private long id;

  @Column(name = "rate")
  private int rate = -1;

  @Column(name = "note", columnDefinition = "VARCHAR(200)")
  private String note;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "employer_id", nullable = false)
  private EmployerEntity employerEntity;

  @ManyToOne(fetch = FetchType.LAZY)
  @JoinColumn(name = "candidate_id", nullable = false)
  private CandidateEntity candidateEntity;
}

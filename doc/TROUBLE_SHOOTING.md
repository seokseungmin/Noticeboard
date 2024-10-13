# Trouble Shooting
프로젝트를 진행하면서 발생한 문제점들과 해결법 서술합니다.

### N+1 문제 해결 사례 (Trouble Shooting)

#### 문제 상황:
프로젝트 초기 설계에서 특정 게시글을 조회하는 API를 구현했을 때, **N+1 문제**가 발생했습니다. 게시글을 조회할 때 해당 게시글의 작성자 정보를 함께 반환하기 위해 `PostEntity`와 `UserEntity`(작성자)가 연관된 데이터를 조회해야 했습니다. 하지만 단순하게 JPA의 `findById()` 메소드를 사용하면, 게시글을 조회한 후 각 게시글의 작성자를 별도의 쿼리로 추가로 조회하게 되어 성능 저하가 발생했습니다.

**예시:**
```java
public PostEntity getPost(Long postId) {
    return postRepository.findById(postId)
        .orElseThrow(() -> new BizException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
}
```

위 코드는 JPA가 게시글을 조회하고, 그 후에 해당 게시글의 작성자 정보를 **추가적인 쿼리**로 조회하는 방식이었습니다. 이 방식은 게시글 목록이나 다수의 엔티티를 처리할 때 **N+1 문제**를 초래하게 되어, DB에 불필요한 쿼리가 다수 발생하고 성능이 크게 저하될 수 있었습니다.

#### 문제 원인:
- **N+1 문제**는 1개의 쿼리로 N개의 엔티티(게시글)를 조회한 후, 각 엔티티와 연관된 엔티티(작성자)를 조회하기 위해 추가로 N개의 쿼리가 실행되는 문제입니다. 이로 인해 **쿼리 개수가 급증**하며 성능 저하를 일으킵니다.

**문제 발생 쿼리 예시 (N+1 문제 발생 시):**
1. 게시글 조회 쿼리
   ```sql
   SELECT * FROM post_entity WHERE id = ?
   ```
2. 각 게시글에 대한 작성자 조회 쿼리 (게시글 개수만큼 추가 실행)
   ```sql
   SELECT * FROM user_entity WHERE id = ?
   ```

#### 해결 방법:
이를 해결하기 위해 **JPQL의 `JOIN FETCH`**를 사용하여, 게시글을 조회할 때 작성자 정보를 **한 번의 쿼리로 함께 조회**하도록 변경했습니다. 이를 통해 불필요한 추가 쿼리 실행을 방지하고, 성능을 최적화할 수 있었습니다.

**해결된 코드:**
```java
public PostEntity getPost(Long postId) {
    return postRepository.findByIdWithAuthor(postId)
        .orElseThrow(() -> new BizException("게시물을 찾을 수 없습니다.", HttpStatus.NOT_FOUND));
}
```

**JPQL 쿼리 예시:**
```java
@Query("SELECT p FROM PostEntity p JOIN FETCH p.author WHERE p.id = :postId")
Optional<PostEntity> findByIdWithAuthor(@Param("postId") Long postId);
```

#### 해결 결과:
- **쿼리 개수 감소**: `JOIN FETCH`를 사용해 게시글과 작성자를 한 번에 가져옴으로써, **N+1 문제를 해결**하고 쿼리 실행 횟수를 크게 줄였습니다.
- **성능 개선**: 불필요한 데이터베이스 호출이 사라져 전체 조회 성능이 개선되었습니다.

**해결된 쿼리 예시 (JOIN FETCH 사용 후):**
```sql
SELECT p.id, p.title, p.content, p.create_date, u.username
FROM post_entity p
JOIN user_entity u ON p.user_id = u.id
WHERE p.id = ?
```

이와 같이 N+1 문제를 해결함으로써, 데이터베이스와의 통신 효율성을 높이고, 프로젝트의 전반적인 성능을 개선할 수 있었습니다.

---

**문제 상황:**
게시글과 그에 대한 댓글을 조회하는 과정에서 **N+1 문제**가 발생했습니다. N+1 문제는, 하나의 엔티티(게시글)를 조회할 때 연관된 엔티티(댓글, 작성자 등)를 별도의 쿼리로 반복 조회하는 문제를 말합니다. 이로 인해 데이터베이스에 불필요한 수많은 쿼리가 실행되어 성능 저하가 발생합니다.

**발생한 문제:**
- 게시글을 조회할 때, 해당 게시글에 대한 **댓글**과 댓글 작성자의 **작성자 정보**를 조회하는 과정에서 N+1 문제가 발생했습니다.
- 게시글 1개에 대해 1개의 쿼리가 실행된 후, 해당 게시글의 댓글 수에 따라 댓글과 작성자 정보를 각각 추가적인 쿼리로 조회하게 되어 불필요한 데이터베이스 쿼리가 다수 발생했습니다.

**해결 과정:**

1. **문제 식별:**
   문제를 발견한 로그에서는 게시글과 관련된 댓글을 조회할 때 각 댓글의 작성자 정보를 별도의 SELECT 문으로 조회하는 방식이었습니다. 이는 댓글 수가 많을수록 많은 추가 쿼리를 발생시키는 전형적인 N+1 문제였습니다.

   예시 로그:
   ```sql
   select ue1_0.id, ue1_0.email, ue1_0.username 
   from user_entity ue1_0 
   where ue1_0.id = ?;  -- 작성자 정보 조회 쿼리가 댓글 수만큼 반복 실행됨
   ```

2. **해결 방법:**
   이를 해결하기 위해 **`JOIN FETCH`**를 사용하여 댓글을 조회할 때 작성자 정보를 함께 가져오도록 쿼리를 수정했습니다.
    - **`JOIN FETCH`**를 사용하면 댓글과 작성자 정보를 한 번에 가져올 수 있습니다. 이를 통해 댓글마다 작성자 정보를 따로 조회하지 않고, 하나의 쿼리로 댓글과 작성자 정보를 한꺼번에 조회할 수 있게 되어 N+1 문제를 해결할 수 있었습니다.

   수정된 쿼리 예시:
   ```sql
   select ce1_0.id, ce1_0.content, a1_0.username 
   from comment_entity ce1_0 
   join user_entity a1_0 on a1_0.id = ce1_0.user_id
   where ce1_0.post_id = ?  -- 작성자 정보를 JOIN FETCH로 함께 가져옴
   ```

3. **최종 해결:**
   수정된 코드로 실행된 로그에서는, **댓글과 작성자 정보가 한 번의 쿼리로 조회**되었음을 확인했습니다. 추가적인 SELECT 쿼리가 발생하지 않아 N+1 문제가 해결되었습니다.

   최종 로그:
   ```sql
   /* SELECT c FROM CommentEntity c JOIN FETCH c.author WHERE c.post.id = :postId */
   select ce1_0.id, ce1_0.user_id, a1_0.username 
   from comment_entity ce1_0 
   join user_entity a1_0 on a1_0.id = ce1_0.user_id 
   where ce1_0.post_id = 1
   order by ce1_0.create_date desc;
   ```

**결과:**
- **N+1 문제 해결**: 게시글과 관련된 댓글 및 작성자 정보를 조회할 때 발생했던 N+1 문제를 해결했습니다.
- **성능 개선**: 여러 쿼리 대신 **단일 쿼리**로 댓글과 작성자 정보를 가져오게 되어 성능이 크게 개선되었습니다.
- **페이지네이션 적용**: 댓글 조회 시 페이징 처리(`limit`)도 함께 적용되어, 대량의 댓글을 효율적으로 처리할 수 있게 되었습니다.

이 내용은 프로젝트의 성능 최적화를 위한 중요한 개선 사례로, 데이터베이스와의 통신 최적화 및 대규모 데이터 처리 시 성능 문제를 해결하는 데 기여했습니다.
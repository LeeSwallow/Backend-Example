package com.pnu.mongotest;

import com.pnu.mongotest.entity.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.data.mongodb.core.MongoTemplate;

import java.util.List;
import java.util.UUID;

@SpringBootTest
class MongoCRUDTest {

    @Autowired
    MongoTemplate mongoTemplate;

    // 1. 컬렉션 이름 출력
    @Test
    void getCollectionNames() {
        System.out.println("컬렉션 목록: " + mongoTemplate.getCollectionNames());
    }

    // 2. 도큐먼트 저장
    @Test
    void insertUser() {
        User user = new User(null, "Alice", 25);
        user.setId(UUID.randomUUID());  // UUID 수동 할당
        mongoTemplate.insert(user);
        System.out.println("도큐먼트 저장 완료");
    }

    // 3. 도큐먼트 조회 (이름 기준)
    @Test
    void findUserByName() {
        Query query = new Query(Criteria.where("name").is("Alice"));
        User result = mongoTemplate.findOne(query, User.class);
        System.out.println("조회 결과: " + result);
    }

    // 4. 도큐먼트 업데이트 (이름 기준)
    @Test
    void updateUserAge() {
        Query query = new Query(Criteria.where("name").is("Alice"));
        Update update = new Update().set("age", 30);
        mongoTemplate.updateFirst(query, update, User.class);
        System.out.println("도큐먼트 업데이트 완료");
    }

    // 5. 도큐먼트 삭제 (이름 기준)
    @Test
    void deleteUserByName() {
        Query query = new Query(Criteria.where("name").is("Alice"));
        mongoTemplate.remove(query, User.class);
        System.out.println("도큐먼트 삭제 완료");
    }

    // 6. 복합 조건 조회 예시
    @Test
    void complexQuery() {
        Query query = new Query();
        query.addCriteria(Criteria.where("age").gte(20).lte(40));
        query.addCriteria(Criteria.where("name").is("Alice"));
        List<User> list = mongoTemplate.find(query, User.class);
        System.out.println("복합 조건 조회 건수: " + list.size());
    }
}

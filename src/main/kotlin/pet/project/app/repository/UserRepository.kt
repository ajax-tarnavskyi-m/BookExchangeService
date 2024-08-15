package pet.project.app.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import pet.project.app.model.User

@Repository
interface UserRepository : MongoRepository <User, String> {

}
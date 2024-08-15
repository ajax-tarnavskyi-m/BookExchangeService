package pet.project.app.repository

import org.springframework.data.mongodb.repository.MongoRepository
import org.springframework.stereotype.Repository
import pet.project.app.repository.model.Book

@Repository
interface BookRepository : MongoRepository <Book, Long> {

}
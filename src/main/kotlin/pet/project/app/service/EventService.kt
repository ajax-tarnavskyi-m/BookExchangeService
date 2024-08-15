package pet.project.app.service

import org.springframework.stereotype.Service
import pet.project.app.repository.model.User
import pet.project.app.repository.UserRepository

@Service
class EventService(val userRepository : UserRepository) {

    fun createEvent(bookId: Long) {
        TODO("Implement method")
//        val subscribers = userRepository.findAllWhereWishListContainsWithId(bookId);
//        for (subscriber in subscribers) {
//            sendEvent(subscriber, bookId)
//        }
    }

    private fun sendEvent(subscriber: User, bookId: Long) {
        println("Ivent was sent for ${subscriber.login} about book with id $bookId")
    }
}
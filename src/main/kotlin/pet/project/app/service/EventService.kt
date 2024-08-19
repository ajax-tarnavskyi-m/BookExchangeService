package pet.project.app.service

import org.springframework.stereotype.Service
import pet.project.app.repository.UserRepository

/**
 * This service is responsible for gathering information about all users who are subscribed to a specific book
 * and sending them a notification message via Kafka.
 *
 * It interacts with the {@link UserRepository} to fetch the relevant user data and uses Kafka messaging to
 * broadcast updates or notifications to the users who are subscribed to the book.
 */
@Service
class EventService(private val userRepository: UserRepository)

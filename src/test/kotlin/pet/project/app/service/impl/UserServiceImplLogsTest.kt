//package pet.project.app.service.impl
//
//import io.mockk.impl.annotations.InjectMockKs
//import io.mockk.impl.annotations.MockK
//import io.mockk.junit5.MockKExtension
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.extension.ExtendWith
//import org.slf4j.LoggerFactory
//import pet.project.app.repository.BookRepository
//import pet.project.app.repository.UserRepository
//
//@ExtendWith(MockKExtension::class)
//class UserServiceImplLogsTest {
//    @MockK
//    private lateinit var bookRepository: BookRepository
//    @MockK
//    private lateinit var userRepository: UserRepository
//    @InjectMockKs
//    lateinit var userService: UserServiceImpl
//
//    @BeforeEach
//    fun setup() {
//        // Устанавливаем захватчик логов для нашего логгера
//        val logger = LoggerFactory.getLogger(UserServiceImpl::class.java) as Logger
//        capturingAppender = CapturingAppender()
//        capturingAppender.start()
//        logger.addAppender(capturingAppender)
//
//        // Мокируем зависимости, если нужно
//        val userRepositoryMock = mockk<UserRepository>()
//        val bookRepositoryMock = mockk<BookRepository>()
//
//        // Создаём экземпляр UserServiceImpl
//        userService = UserServiceImpl(userRepositoryMock, bookRepositoryMock)
//    }
//
//    @Test
//    fun `should log message when addBookToWishList is called`() {
//        // Given
//        val userId = "someUserId"
//        val bookId = "someBookId"
//
//        every { bookRepositoryMock.existsById(bookId) } returns true
//        every { userRepositoryMock.addBookToWishList(userId, bookId) } returns 1L
//
//        // When
//        userService.addBookToWishList(userId, bookId)
//
//        // Then
//        val logMessages = capturingAppender.getMessages()
//        assertThat(logMessages).contains("Successfully added book to wishlist for user with id=$userId")
//    }
//}

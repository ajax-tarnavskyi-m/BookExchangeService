//package pet.project.app.controller
//
//import com.fasterxml.jackson.databind.ObjectMapper
//import com.ninjasquad.springmockk.MockkBean
//import io.mockk.every
//import io.mockk.just
//import io.mockk.runs
//import io.mockk.verify
//import org.bson.types.ObjectId
//import org.junit.jupiter.api.BeforeEach
//import org.junit.jupiter.api.Test
//import org.springframework.beans.factory.annotation.Autowired
//import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest
//import org.springframework.http.MediaType
//import org.springframework.test.web.servlet.MockMvc
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.status
//import pet.project.app.dto.book.BookMapper.toModel
//import pet.project.app.dto.book.CreateBookRequest
//import pet.project.app.dto.book.ResponseBookDto
//import pet.project.app.dto.book.UpdateAmountRequest
//import pet.project.app.dto.book.UpdateBookRequest
//import pet.project.app.model.Book
//import pet.project.app.service.BookService
//import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
//import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
//
//@WebMvcTest(BookController::class)
//class BookControllerTest {
//    @Autowired
//    private lateinit var mockMvc: MockMvc
//
//    @MockkBean
//    private lateinit var bookService: BookService
//
//    @Autowired
//    private lateinit var objectMapper: ObjectMapper
//
//    private lateinit var initializedBook: Book
//    private lateinit var createBookRequest: CreateBookRequest
//    private lateinit var updateBookRequest: UpdateBookRequest
//    private lateinit var updateAmountRequest: UpdateAmountRequest
//    private lateinit var responseBookDto: ResponseBookDto
//
//    @BeforeEach
//    fun setUp() {
//        initializedBook = Book(ObjectId("66bf6bf8039339103054e21a"), "Title", "Description", 2023, 20.0, 10)
//        createBookRequest = CreateBookRequest("Title", "Description", 2023, 20.0, 10)
//        updateBookRequest = UpdateBookRequest("66bf6bf8039339103054e21a", "Updated Title", "Updated Description", 2023, 25.0, 15)
//        updateAmountRequest = UpdateAmountRequest(5)
//        responseBookDto = ResponseBookDto("66bf6bf8039339103054e21a", "Title", "Description", 2023, 20.0, 10)
//    }
//
//    @Test
//    fun `get book by id successfully`() {
//        //GIVEN
//        val book = initializedBook
//
//        //WHEN
//        every { bookService.getById("66bf6bf8039339103054e21a") } returns book
//        mockMvc.perform(get("/book/{id}", "66bf6bf8039339103054e21a"))
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.title").value(responseBookDto.title))
//            .andExpect(jsonPath("$.description").value(responseBookDto.description))
//
//        //THEN
//        verify { bookService.getById("66bf6bf8039339103054e21a") }
//    }
//
//    @Test
//    fun `create book successfully`() {
//        // GIVEN
//        val book = createBookRequest.toModel()
//        every { bookService.create(book) } returns initializedBook
//
//        // WHEN
//        mockMvc.perform(
//            post("/book/")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(createBookRequest))
//        )
//            .andExpect(status().isCreated)
//            .andExpect(jsonPath("$.title").value(createBookRequest.title))
//            .andExpect(jsonPath("$.description").value(createBookRequest.description))
//
//        // THEN
//        verify { bookService.create(book) }
//    }
//
//    @Test
//    fun `update book successfully`() {
//        // GIVEN
//        val book = updateBookRequest.toModel()
//        every { bookService.update(any()) } returns book
//
//        // WHEN
//        mockMvc.perform(
//            put("/book/")
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(updateBookRequest))
//        )
//            .andExpect(status().isOk)
//            .andExpect(jsonPath("$.title").value(updateBookRequest.title))
//            .andExpect(jsonPath("$.description").value(updateBookRequest.description))
//
//        // THEN
//        verify { bookService.update(any()) }
//    }
//
//    @Test
//    fun `update book amount successfully`() {
//        // GIVEN
//        val bookId = "66bf6bf8039339103054e21a"
//        val newAmount = 15
//
//        every { bookService.changeAmount(bookId, updateAmountRequest.delta) } returns newAmount
//
//        // WHEN
//        mockMvc.perform(
//            patch("/book/{id}/amount", bookId)
//                .contentType(MediaType.APPLICATION_JSON)
//                .content(objectMapper.writeValueAsString(updateAmountRequest))
//        )
//            .andExpect(status().isOk)
//            .andExpect(content().string(newAmount.toString()))
//
//        // THEN
//        verify { bookService.changeAmount(bookId, updateAmountRequest.delta) }
//    }
//
//    @Test
//    fun `delete book successfully`() {
//        // GIVEN
//        val bookId = "66bf6bf8039339103054e21a"
//
//        every { bookService.delete(bookId) } just runs
//
//        // WHEN
//        mockMvc.perform(delete("/book/{id}", bookId))
//            .andExpect(status().isNoContent)
//
//        // THEN
//        verify { bookService.delete(bookId) }
//    }
//}

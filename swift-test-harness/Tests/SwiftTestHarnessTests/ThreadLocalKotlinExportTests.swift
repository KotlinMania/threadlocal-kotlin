import Testing
import Threadlocal

@Suite("Threadlocal Swift Export Tests")
struct ThreadlocalExportTests {
    @Test("Threadlocal Swift module imports cleanly")
    func swiftModuleLoads() {
        #expect(true)
    }
}

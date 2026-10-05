import Testing
import Threadlocal

@Suite("Threadlocal Swift Export Tests")
struct ThreadlocalExportTests {
    @Test("Threadlocal Swift module imports cleanly")
    func swiftModuleLoads() {
        #expect(Bool(true))
    }

    @Test("Cached thread-local constructors and exact iterator sizes")
    func cachedThreadLocalIterators() {
        let locals = [CachedThreadLocal(), CachedThreadLocal.Companion.shared.new(),
                      CachedThreadLocal.Companion.shared.default()]
        for local in locals {
            #expect(local.get() == nil)
            let mutable = local.iterMut()
            #expect(!mutable.hasNext())
            #expect(mutable.len() == 0)
            #expect(mutable.sizeHint().lower == 0)
            #expect(mutable.sizeHint().upper == 0)
            let consuming = local.intoIter()
            #expect(!consuming.hasNext())
            #expect(consuming.len() == 0)
            #expect(consuming.sizeHint().lower == 0)
            #expect(consuming.sizeHint().upper == 0)
            local.clear()
        }
    }
}

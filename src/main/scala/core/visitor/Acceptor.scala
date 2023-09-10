package core

import window.WindowInfo

trait Acceptor[A]:
    def accept(visitor: A): A
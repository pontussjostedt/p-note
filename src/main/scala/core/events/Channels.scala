package core

sealed trait EventChannels
case object TransformChannel extends EventChannels
case object AddedChannel extends EventChannels
case object RemovedChannel extends EventChannels
case object UpdatedChannel extends EventChannels

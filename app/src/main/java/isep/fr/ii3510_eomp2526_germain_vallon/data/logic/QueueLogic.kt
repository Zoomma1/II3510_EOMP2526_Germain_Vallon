package isep.fr.ii3510_eomp2526_germain_vallon.data.logic

import isep.fr.ii3510_eomp2526_germain_vallon.data.model.QueueItem

/*
    Logic functions for queue manipulation
    This file is kept separate from ViewModel to allow unit testing
 */

fun nowPlaying(queue: List<QueueItem>): QueueItem? =
    queue.firstOrNull { it.status == "PLAYING" }

fun upNext(queue: List<QueueItem>): List<QueueItem> =
    queue.filter { it.status == "QUEUED" }

fun history(queue: List<QueueItem>): List<QueueItem> =
    queue.filter { it.status == "DONE" }

fun containsActiveTrack(queue: List<QueueItem>, trackId: String): Boolean =
    queue.any { it.track.trackId == trackId && it.status != "DONE" }

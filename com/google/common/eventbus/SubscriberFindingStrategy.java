package com.google.common.eventbus;

import com.google.common.collect.Multimap;

interface SubscriberFindingStrategy {
  Multimap<Class<?>, EventSubscriber> findAllSubscribers(Object paramObject);
}

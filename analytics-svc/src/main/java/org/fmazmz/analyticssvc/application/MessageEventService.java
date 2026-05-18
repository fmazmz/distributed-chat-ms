package org.fmazmz.analyticssvc.application;

import org.fmazmz.analyticssvc.model.MessageSentEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MessageEventService {

    @Transactional
    public void process(MessageSentEvent event) {
        //
    }
}

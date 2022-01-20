package com.github.taccisum.pigeon.core.repo;

import com.github.taccisum.domain.core.exception.DataNotFoundException;
import com.github.taccisum.pigeon.core.dao.MassTacticDAO;
import com.github.taccisum.pigeon.core.data.MassTacticDO;
import com.github.taccisum.pigeon.core.entity.core.MassTactic;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Optional;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Component
public class MassTacticRepo {
    @Resource
    private MassTacticDAO dao;
    @Resource
    private Factory factory;

    public MassTactic create(MassTacticDO data) {
        data.setStatus(MassTactic.Status.AVAILABLE);
        Long id = dao.insert(data);
        return factory.createMassTactic(id, data.getType());
    }

    public Optional<MassTactic> get(long id) {
        MassTacticDO data = dao.selectById(id);
        if (data == null) {
            return Optional.empty();
        }
        return Optional.of(factory.createMassTactic(id, data.getType()));
    }

    public MassTactic getOrThrow(long id) throws NotFoundException {
        return this.get(id)
                .orElseThrow(() -> new NotFoundException(id));
    }

    public static class NotFoundException extends DataNotFoundException {
        public NotFoundException(long id) {
            super("群发策略", id);
        }
    }
}

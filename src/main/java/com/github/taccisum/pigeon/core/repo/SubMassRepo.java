package com.github.taccisum.pigeon.core.repo;

import com.github.taccisum.pigeon.core.dao.SubMassDAO;
import com.github.taccisum.pigeon.core.data.SubMassDO;
import com.github.taccisum.pigeon.core.entity.core.SubMass;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.1
 */
@Component
public class SubMassRepo {
    @Resource
    private SubMassDAO dao;
    @Resource
    private Factory factory;

    public SubMass create(long massId, int serialNum, int start, int size) {
        SubMassDO data = dao.newEmptyDataObject();
        data.setMainId(massId);
        data.setStatus(SubMass.Status.INIT);
        data.setSerialNum(serialNum);
        data.setSize(size);
        data.setStart(start);
        Long id = dao.insert(data);
        return factory.createSubMessageMass(id);
    }

    public Optional<SubMass> get(Long id) {
        SubMassDO data = dao.selectById(id);
        if (data == null) {
            return Optional.empty();
        }
        return Optional.of(factory.createSubMessageMass(data.getId()));
    }

    public List<SubMass> listByMainId(Long mainId) {
        return dao.selectByMainId(mainId)
                .stream()
                .map(data -> factory.createSubMessageMass(data.getId()))
                .collect(Collectors.toList());
    }
}

package pigeon.core.docs.err;

import com.github.taccisum.domain.core.DomainException;
import com.github.taccisum.domain.core.exception.ErrorCode;
import com.github.taccisum.domain.core.exception.ErrorCodeMapping;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.util.ClassUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author taccisum - liaojinfeng6938@dingtalk.com
 * @since 0.2
 */
public class ErrorCodeFinder {
    private List<String> packages = new ArrayList<>();
    private ErrorCodeMapping errorCodeMapping;

    public ErrorCodeFinder(List<String> packages, ErrorCodeMapping errorCodeMapping) {
        this.packages = packages;
        this.errorCodeMapping = errorCodeMapping;
    }

    public List<ErrorCode> findAll() {
        List<ErrorCode> all = packages.stream()
                .map(pkg -> {
                    try {
                        return getClasspath(pkg);
                    } catch (Exception e) {
                        return null;
                    }
                })
                .filter(classes -> classes != null)
                .flatMap(classes -> classes.stream())
                .map(clazz -> {
                    if (DomainException.class.isAssignableFrom(clazz)) {
                        return errorCodeMapping.map((Class<? extends DomainException>) clazz);
                    }
                    return null;
                })
                .filter(errcode -> errcode != null)
                .collect(Collectors.toList());

        return all;
    }

    public static List<Class<?>> getClasspath(String packagePath) throws Exception {
        ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
        MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
        // 加载系统所有类资源
        Resource[] resources = resourcePatternResolver.getResources("classpath*:" + packagePath.replaceAll("[.]", "/") + "/**/*.class");
        List<Class<?>> list = new ArrayList<Class<?>>();
        // 把每一个class文件找出来
        for (Resource r : resources) {
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(r);
            Class<?> clazz = ClassUtils.forName(metadataReader.getClassMetadata().getClassName(), null);
            list.add(clazz);
        }
        return list;
    }
}

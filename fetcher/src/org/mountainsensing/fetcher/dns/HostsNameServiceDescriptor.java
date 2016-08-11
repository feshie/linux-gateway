package org.mountainsensing.fetcher.dns;

import sun.net.spi.nameservice.NameService;
import sun.net.spi.nameservice.NameServiceDescriptor;

/**
 * NameServiceDescriptor for the {@link HostsNameService}.
 * 
 * This describes the name and type of the service,
 * and a means by which it can be instantiated.
 * 
 * To allow {@link HostsNameService} to be enabled,
 * the fully qualifed name of this class must be present in:
 * {@code META-INF/services/sun.net.spi.nameservice.NameServiceDescriptor}
 * 
 * This allows {@link HostsNameService} to be found / instantiated auto-magically
 * if it set to be used - see {@link HostsNameService#enable}.
 */
public class HostsNameServiceDescriptor implements NameServiceDescriptor {

    @Override
    public NameService createNameService() throws Exception {
        return new HostsNameService();
    }

    @Override
    public String getProviderName() {
        return HostsNameService.NAME;
    }

    @Override
    public String getType() {
        return HostsNameService.TYPE;
    }
}
import { HALResource } from './../src/HALResource';
import { HalLink } from 'hal-types';

export type TestResource<T extends HALResource = HALResource> = {
	_links?:{
		someLink?: HalLink;
	};
    additionalField?: string;
} & T;

import { HALResource } from './../src/HALResource';
import { HalLink } from 'hal-types';

export type TestResource = HALResource & {
	_links?:{
		someLink?: HalLink;
	};
	additionalField?: string;
	someDate?: Date;
}

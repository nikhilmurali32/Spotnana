import React from 'react';

export interface FlightSegment {
  flightNumber: string;
  airlineCode: string;
  departureAirportCode: string;
  arrivalAirportCode: string;
  departureTime: string;
  arrivalTime: string;
  durationMinutes: number;
}

export interface Itinerary {
  segments: FlightSegment[];
  totalTravelTimeMinutes: number;
  totalPrice: number;
}

function formatDuration(minutes: number) {
  const h = Math.floor(minutes / 60);
  const m = minutes % 60;
  return `${h}h ${m}m`;
}

function extractLocalTime(isoString: string) {
  try {
    const match = isoString.match(/T(\d{2}:\d{2})/);
    if (match) return match[1];
    return new Date(isoString).toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' });
  } catch {
    return isoString;
  }
}

export default function FlightItineraryCard({ itinerary }: { itinerary: Itinerary }) {
  return (
    <div className="bg-slate-800/50 backdrop-blur-md rounded-2xl shadow-xl border border-slate-700 p-6 hover:border-blue-500/50 transition-all">
      {/* Header */}
      <div className="flex justify-between items-center mb-6 pb-4 border-b border-slate-700/80">
        <div className="text-3xl font-extrabold text-emerald-400 tracking-tight">
          ${itinerary.totalPrice.toFixed(2)}
        </div>
        <div className="text-slate-300 font-semibold flex items-center gap-2 bg-slate-900/50 px-4 py-2 rounded-lg border border-slate-700">
          <svg className="w-5 h-5 text-blue-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
            <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M12 8v4l3 3m6-3a9 9 0 11-18 0 9 9 0 0118 0z" />
          </svg>
          {formatDuration(itinerary.totalTravelTimeMinutes)}
        </div>
      </div>

      {/* Segments */}
      <div className="space-y-2">
        {itinerary.segments.map((segment, index) => {
          const isLast = index === itinerary.segments.length - 1;
          let layoverBadge = null;
          
          if (!isLast) {
            const nextSegment = itinerary.segments[index + 1];
            const arrTime = new Date(segment.arrivalTime).getTime();
            const depTime = new Date(nextSegment.departureTime).getTime();
            const layoverMins = Math.round((depTime - arrTime) / 60000);
            
            layoverBadge = (
              <div className="flex justify-center my-1 relative py-3">
                <div className="absolute inset-0 flex items-center">
                  <div className="w-full border-t border-slate-600/60 border-dashed"></div>
                </div>
                <div className="relative bg-slate-800 px-4 py-1.5 rounded-full border border-slate-600 text-xs font-semibold text-slate-300 shadow-sm flex items-center gap-1.5">
                  <svg className="w-3.5 h-3.5 text-slate-400" fill="none" viewBox="0 0 24 24" stroke="currentColor">
                    <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M5 8h14M5 8a2 2 0 110-4h14a2 2 0 110 4M5 8v10a2 2 0 002 2h10a2 2 0 002-2V8m-9 4h4" />
                  </svg>
                  Layover: {formatDuration(layoverMins)}
                </div>
              </div>
            );
          }

          return (
            <React.Fragment key={index}>
              <div className="flex flex-col sm:flex-row items-center justify-between p-4 bg-slate-900/60 rounded-xl border border-slate-700/60 shadow-inner gap-6 sm:gap-0">
                {/* Airline & Flight Info */}
                <div className="flex items-center gap-4 w-full sm:w-1/3">
                  <div className="bg-gradient-to-br from-blue-500/20 to-purple-500/20 p-3 rounded-xl border border-blue-500/20 text-blue-400 shadow-sm">
                    <svg className="w-6 h-6 transform rotate-45" fill="currentColor" viewBox="0 0 24 24">
                      <path d="M21 16v-2l-8-5V3.5c0-.83-.67-1.5-1.5-1.5S10 2.67 10 3.5V9l-8 5v2l8-2.5V19l-2 1.5V22l3.5-1 3.5 1v-1.5L13 19v-5.5l8 2.5z" />
                    </svg>
                  </div>
                  <div>
                    <div className="font-bold text-white text-lg tracking-wide">{segment.airlineCode}</div>
                    <div className="text-xs font-medium text-slate-400 tracking-wider">FLIGHT {segment.flightNumber}</div>
                  </div>
                </div>

                {/* Flight Route Visual */}
                <div className="flex items-center justify-between w-full sm:w-2/3 sm:pl-6">
                  <div className="text-center min-w-[60px]">
                    <div className="text-2xl font-black text-white">{extractLocalTime(segment.departureTime)}</div>
                    <div className="text-sm font-semibold text-slate-400">{segment.departureAirportCode}</div>
                  </div>
                  
                  <div className="flex-1 px-4 flex flex-col items-center justify-center">
                    <div className="text-[11px] font-bold text-slate-500 mb-1.5 uppercase tracking-widest">{formatDuration(segment.durationMinutes)}</div>
                    <div className="w-full h-[2px] bg-slate-600/50 relative rounded-full">
                      <div className="absolute right-0 -top-[4px] w-2.5 h-2.5 rounded-full bg-blue-400 shadow-[0_0_8px_rgba(96,165,250,0.8)]"></div>
                      <div className="absolute left-0 -top-[4px] w-2.5 h-2.5 rounded-full border-2 border-slate-400 bg-slate-900"></div>
                    </div>
                  </div>

                  <div className="text-center min-w-[60px]">
                    <div className="text-2xl font-black text-white">{extractLocalTime(segment.arrivalTime)}</div>
                    <div className="text-sm font-semibold text-slate-400">{segment.arrivalAirportCode}</div>
                  </div>
                </div>
              </div>
              {layoverBadge}
            </React.Fragment>
          );
        })}
      </div>
    </div>
  );
}

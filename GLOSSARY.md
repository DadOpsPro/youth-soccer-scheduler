# Youth Soccer Schedule Coordinator

Finds the places where a multi-team youth soccer schedule contradicts itself, and explains each one in plain language.

## Language

### People

**Coordinator**:
The person who keeps the schedules of several Teams consistent with each other. A Coach who checks their own bookings is acting as a Coordinator while they do it.
_Avoid_: Operator, manager, admin, user

**Coach**:
A person assigned to lead an Event. A Coach is the same person in every Season they appear in.
_Avoid_: Trainer, instructor, staff

### Places

**Venue**:
A physical location that holds one or more Fields, such as Scheels Soccer Complex.
_Avoid_: Complex, site, facility, location

**Field**:
A single playing surface at a Venue that can be booked for one Event at a time, such as Field 1. A Field is the same Field in every Season that uses it.
_Avoid_: Pitch, venue, surface

### Teams and events

**Team**:
A youth team whose schedule is being coordinated, such as U10 Blue. A Team belongs to one Season, because its roster changes from Season to Season.
_Avoid_: Squad, home team, my team

**Opponent**:
The team a Team plays against in a Game. It may belong to an outside club, or it may be another Team being coordinated.
_Avoid_: Away team, visitor, rival

**Age Group**:
The band of player ages that a Team or Tryout is for, such as U12.
_Avoid_: Division, bracket, level

**Event**:
A Game, Practice or Tryout: something that occupies a Field and a Coach during a set window of time.
_Avoid_: Booking, slot, appointment

**Game**:
An Event in which a Team plays an Opponent.
_Avoid_: Match, fixture

**Practice**:
An Event in which a Team trains, with no Opponent.
_Avoid_: Training, session

**Tryout**:
An Event at which players are assessed for the Teams of one Age Group. It belongs to the Age Group, not to a Team.
_Avoid_: Evaluation, assessment, combine

**Home / Away**:
The side a Team is designated in a Game. It decides which Kit the Team wears, and it is set for each Game rather than worked out from the Venue.
_Avoid_: Host, visiting

**Kit**:
The uniform a Team wears. Each Team has a home kit and an away kit.
_Avoid_: Uniform, jersey, strip

### Schedules

**Season**:
A named stretch of the year that a set of Events belongs to, such as Spring 2026. Seasons can overlap: a winter indoor league can still be running while spring tryouts begin.
_Avoid_: Term, session, year

**Schedule**:
Every Event the Coordinator is keeping consistent, across all Teams, for one Season. Overlapping Seasons each have their own Schedule. One Team's or one Coach's schedule is a view of the Schedule, not a separate thing.
_Avoid_: Calendar, fixture list

### Conflicts

**Conflict**:
Two Events that need the same Coach or the same Field at overlapping times, whichever Seasons they belong to. Events that only touch, where one ends exactly as the other begins, do not conflict.
_Avoid_: Clash, collision, error

**Coach double-booking**:
A Conflict where one Coach is expected at two overlapping Events.
_Avoid_: Coach clash

**Field double-booking**:
A Conflict where one Field is used by two overlapping Events.
_Avoid_: Venue double-booking, field clash
